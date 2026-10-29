package com.pict.wallet.controller;

import com.pict.wallet.dto.*;
import com.pict.wallet.model.TransactionStatus;
import com.pict.wallet.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class WalletApiController {
    private final UserService users;
    private final WalletService wallets;

    public WalletApiController(UserService users, WalletService wallets) {
        this.users = users;
        this.wallets = wallets;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrf) {
        return Map.of("token", csrf.getToken(), "headerName", csrf.getHeaderName(), "parameterName", csrf.getParameterName());
    }

    @PostMapping("/register")
    public ResponseEntity<WalletResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(users.register(request));
    }

    @GetMapping("/wallet")
    public WalletResponse wallet(Principal principal) { return users.getWallet(principal.getName()); }

    @PostMapping("/wallet/add-money")
    public ResponseEntity<TransactionResponse> addMoney(Principal principal, @Valid @RequestBody AddMoneyRequest request) {
        return response(wallets.addMoney(principal.getName(), request.getAmount()));
    }

    @PostMapping("/wallet/transfer")
    public ResponseEntity<TransactionResponse> transfer(Principal principal, @Valid @RequestBody TransferRequest request) {
        // Sender comes from the authenticated session, never from a request field.
        return response(wallets.transfer(principal.getName(), request.getRecipientEmail(), request.getAmount()));
    }

    @GetMapping("/wallet/transactions")
    public List<TransactionResponse> history(Principal principal) { return wallets.history(principal.getName()); }

    private ResponseEntity<TransactionResponse> response(TransactionResponse transaction) {
        HttpStatus status = transaction.getStatus() == TransactionStatus.SUCCESS
                ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_CONTENT;
        return ResponseEntity.status(status).body(transaction);
    }
}
