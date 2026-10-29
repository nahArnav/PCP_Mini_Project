package com.pict.wallet.service;

import com.pict.wallet.dto.TransactionResponse;
import com.pict.wallet.exception.WalletException;
import com.pict.wallet.model.*;
import com.pict.wallet.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class WalletService {
    public static final BigDecimal MAX_BALANCE = new BigDecimal("1000000.00");
    private final UserRepository users;
    private final TransactionRepository transactions;

    public WalletService(UserRepository users, TransactionRepository transactions) {
        this.users = users;
        this.transactions = transactions;
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.compareTo(new BigDecimal("100000.00")) > 0) {
            throw new WalletException(HttpStatus.BAD_REQUEST, "Amount must be between INR 0.01 and INR 100000");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new WalletException(HttpStatus.BAD_REQUEST, "Use at most two decimal places");
        }
    }

    private WalletUser findUser(String email) {
        return users.findByEmail(UserService.normalizeEmail(email))
                .orElseThrow(() -> new WalletException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private WalletUser lock(Long id) {
        return users.findByIdForUpdate(id)
                .orElseThrow(() -> new WalletException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private TransactionResponse record(WalletUser user, WalletUser recipient, String recipientEmail,
                                       BigDecimal amount, TransactionType type, TransactionStatus status, String message) {
        WalletTransaction transaction = new WalletTransaction(user, recipient, recipientEmail, amount, type, status, message);
        return new TransactionResponse(transactions.saveAndFlush(transaction));
    }

    @Transactional
    public TransactionResponse addMoney(String email, BigDecimal requestedAmount) {
        BigDecimal amount = validateAmount(requestedAmount);
        Long userId = users.findIdByEmail(UserService.normalizeEmail(email))
                .orElseThrow(() -> new WalletException(HttpStatus.NOT_FOUND, "Account not found"));
        // Look up only the ID first, so an unlocked balance is never cached by JPA.
        WalletUser user = lock(userId);
        if (user.getBalance().add(amount).compareTo(MAX_BALANCE) > 0) {
            return record(user, null, null, amount, TransactionType.ADD_MONEY,
                    TransactionStatus.FAILED, "Wallet balance limit is INR 1000000");
        }
        user.setBalance(user.getBalance().add(amount));
        return record(user, null, null, amount, TransactionType.ADD_MONEY,
                TransactionStatus.SUCCESS, "Demo money added successfully");
    }

    @Transactional
    public TransactionResponse transfer(String email, String recipientEmail, BigDecimal requestedAmount) {
        BigDecimal amount = validateAmount(requestedAmount);
        String targetEmail = UserService.normalizeEmail(recipientEmail);
        if (targetEmail.isEmpty() || targetEmail.length() > 120) {
            throw new WalletException(HttpStatus.BAD_REQUEST, "Enter a valid recipient email");
        }
        Long senderId = users.findIdByEmail(UserService.normalizeEmail(email))
                .orElseThrow(() -> new WalletException(HttpStatus.NOT_FOUND, "Account not found"));
        Long recipientId = users.findIdByEmail(targetEmail).orElse(null);

        if (recipientId == null || senderId.equals(recipientId)) {
            WalletUser sender = lock(senderId);
            String reason = recipientId == null ? "Recipient account does not exist" : "You cannot transfer to yourself";
            return record(sender, null, targetEmail, amount, TransactionType.TRANSFER, TransactionStatus.FAILED, reason);
        }

        // Always lock the smaller ID first. Opposite-direction transfers use the same order.
        Long firstId = Math.min(senderId, recipientId);
        Long secondId = Math.max(senderId, recipientId);
        WalletUser first = lock(firstId);
        WalletUser second = lock(secondId);
        WalletUser sender = first.getId().equals(senderId) ? first : second;
        WalletUser recipient = first.getId().equals(recipientId) ? first : second;

        if (sender.getBalance().compareTo(amount) < 0) {
            return record(sender, recipient, targetEmail, amount, TransactionType.TRANSFER,
                    TransactionStatus.FAILED, "Insufficient balance");
        }
        if (recipient.getBalance().add(amount).compareTo(MAX_BALANCE) > 0) {
            return record(sender, recipient, targetEmail, amount, TransactionType.TRANSFER,
                    TransactionStatus.FAILED, "Recipient wallet balance limit would be exceeded");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));
        // Both balances and this record commit together; an unexpected error rolls everything back.
        return record(sender, recipient, targetEmail, amount, TransactionType.TRANSFER,
                TransactionStatus.SUCCESS, "Transfer completed successfully");
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(String email) {
        return transactions.findHistory(findUser(email).getId()).stream()
                .map(TransactionResponse::new).toList();
    }
}
