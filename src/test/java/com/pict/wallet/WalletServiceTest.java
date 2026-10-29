package com.pict.wallet;

import com.pict.wallet.dto.*;
import com.pict.wallet.exception.WalletException;
import com.pict.wallet.model.TransactionStatus;
import com.pict.wallet.repository.*;
import com.pict.wallet.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class WalletServiceTest {
    @Autowired UserService users;
    @Autowired WalletService wallets;
    @Autowired UserRepository userRepository;
    @Autowired TransactionRepository transactionRepository;
    @Autowired PasswordEncoder encoder;
    @Autowired TransactionTemplate transactionTemplate;

    @BeforeEach
    void prepare() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        register("Aadi", "aadi@example.com");
        register("Arnav", "arnav@example.com");
    }

    private WalletResponse register(String name, String email) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword("Classroom123!");
        return users.register(request);
    }

    private void balance(String email, String expected) {
        assertThat(users.getWallet(email).getBalance()).isEqualByComparingTo(expected);
    }

    @Test
    void registrationStartsAtZeroAndHashesPassword() {
        balance("aadi@example.com", "0.00");
        var user = userRepository.findByEmail("aadi@example.com").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo("Classroom123!");
        assertThat(encoder.matches("Classroom123!", user.getPasswordHash())).isTrue();
    }

    @Test
    void duplicateEmailIsCaseInsensitive() {
        assertThatThrownBy(() -> register("Other", " AADI@EXAMPLE.COM "))
                .isInstanceOf(WalletException.class).hasMessageContaining("already exists");
    }

    @Test
    void addMoneyAndTransferCommitCorrectBalances() {
        assertThat(wallets.addMoney("aadi@example.com", new BigDecimal("1000.00")).getStatus())
                .isEqualTo(TransactionStatus.SUCCESS);
        var transfer = wallets.transfer("aadi@example.com", "ARNAV@example.com", new BigDecimal("250.50"));
        assertThat(transfer.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(transfer.getCreatedAt()).isNotNull();
        balance("aadi@example.com", "749.50");
        balance("arnav@example.com", "250.50");
        assertThat(wallets.history("aadi@example.com")).hasSize(2);
        assertThat(wallets.history("arnav@example.com")).hasSize(1);
    }

    @Test
    void insufficientBalanceIsRecordedWithoutMovingMoney() {
        wallets.addMoney("aadi@example.com", new BigDecimal("100.00"));
        var attempt = wallets.transfer("aadi@example.com", "arnav@example.com", new BigDecimal("150.00"));
        assertThat(attempt.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(attempt.getMessage()).isEqualTo("Insufficient balance");
        balance("aadi@example.com", "100.00");
        balance("arnav@example.com", "0.00");
        assertThat(wallets.history("aadi@example.com").get(0).getId()).isEqualTo(attempt.getId());
        assertThat(wallets.history("arnav@example.com")).isEmpty();
    }

    @Test
    void unknownRecipientAndSelfTransferAreRecordedAsFailed() {
        assertThat(wallets.transfer("aadi@example.com", "missing@example.com", BigDecimal.ONE).getStatus())
                .isEqualTo(TransactionStatus.FAILED);
        assertThat(wallets.transfer("aadi@example.com", "aadi@example.com", BigDecimal.ONE).getStatus())
                .isEqualTo(TransactionStatus.FAILED);
        balance("aadi@example.com", "0.00");
        assertThat(wallets.history("aadi@example.com")).hasSize(2);
    }

    @Test
    void invalidAmountsAreRejectedBeforeCreatingATransaction() {
        for (String amount : List.of("0", "-1", "1.001", "100000.01")) {
            assertThatThrownBy(() -> wallets.addMoney("aadi@example.com", new BigDecimal(amount)))
                    .isInstanceOf(WalletException.class);
        }
        assertThatThrownBy(() -> wallets.addMoney("aadi@example.com", null)).isInstanceOf(WalletException.class);
        balance("aadi@example.com", "0.00");
        assertThat(transactionRepository.count()).isZero();
    }

    @Test
    void decimalAmountsStayExact() {
        wallets.addMoney("aadi@example.com", new BigDecimal("0.10"));
        wallets.addMoney("aadi@example.com", new BigDecimal("0.20"));
        balance("aadi@example.com", "0.30");
    }

    @Test
    void maximumBalanceRejectsTopUpAndIncomingTransfer() {
        for (int i = 0; i < 10; i++) {
            wallets.addMoney("arnav@example.com", new BigDecimal("100000.00"));
        }
        var topUp = wallets.addMoney("arnav@example.com", BigDecimal.ONE);
        assertThat(topUp.getStatus()).isEqualTo(TransactionStatus.FAILED);
        wallets.addMoney("aadi@example.com", new BigDecimal("100.00"));
        var transfer = wallets.transfer("aadi@example.com", "arnav@example.com", BigDecimal.ONE);
        assertThat(transfer.getStatus()).isEqualTo(TransactionStatus.FAILED);
        balance("aadi@example.com", "100.00");
        balance("arnav@example.com", "1000000.00");
    }

    @Test
    void rollbackRestoresBothBalancesAndRemovesTransaction() {
        wallets.addMoney("aadi@example.com", new BigDecimal("100.00"));
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            wallets.transfer("aadi@example.com", "arnav@example.com", new BigDecimal("25.00"));
            throw new IllegalStateException("Simulated failure before commit");
        })).isInstanceOf(IllegalStateException.class);
        balance("aadi@example.com", "100.00");
        balance("arnav@example.com", "0.00");
        assertThat(transactionRepository.count()).isEqualTo(1);
    }

    @Test
    void simultaneousTransfersCannotOverspend() throws Exception {
        wallets.addMoney("aadi@example.com", new BigDecimal("100.00"));
        List<TransactionResponse> results = runTogether(List.of(
                () -> wallets.transfer("aadi@example.com", "arnav@example.com", new BigDecimal("80.00")),
                () -> wallets.transfer("aadi@example.com", "arnav@example.com", new BigDecimal("80.00"))));
        assertThat(results.stream().filter(t -> t.getStatus() == TransactionStatus.SUCCESS).count()).isEqualTo(1);
        assertThat(results.stream().filter(t -> t.getStatus() == TransactionStatus.FAILED).count()).isEqualTo(1);
        balance("aadi@example.com", "20.00");
        balance("arnav@example.com", "80.00");
    }

    @Test
    void simultaneousTopUpsDoNotLoseUpdates() throws Exception {
        List<Callable<TransactionResponse>> jobs = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            jobs.add(() -> wallets.addMoney("aadi@example.com", new BigDecimal("0.10")));
        }
        assertThat(runTogether(jobs)).allMatch(t -> t.getStatus() == TransactionStatus.SUCCESS);
        balance("aadi@example.com", "0.80");
    }

    @Test
    void oppositeDirectionTransfersUseConsistentLockOrder() throws Exception {
        wallets.addMoney("aadi@example.com", new BigDecimal("100.00"));
        wallets.addMoney("arnav@example.com", new BigDecimal("100.00"));
        List<Callable<TransactionResponse>> jobs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            jobs.add(() -> wallets.transfer("aadi@example.com", "arnav@example.com", BigDecimal.ONE));
            jobs.add(() -> wallets.transfer("arnav@example.com", "aadi@example.com", BigDecimal.ONE));
        }
        assertThat(runTogether(jobs)).allMatch(t -> t.getStatus() == TransactionStatus.SUCCESS);
        balance("aadi@example.com", "100.00");
        balance("arnav@example.com", "100.00");
    }

    private List<TransactionResponse> runTogether(List<Callable<TransactionResponse>> jobs) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(jobs.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<TransactionResponse>> futures = new ArrayList<>();
            for (var job : jobs) {
                futures.add(executor.submit(() -> { start.await(); return job.call(); }));
            }
            start.countDown();
            List<TransactionResponse> results = new ArrayList<>();
            for (var future : futures) { results.add(future.get(20, TimeUnit.SECONDS)); }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }
}
