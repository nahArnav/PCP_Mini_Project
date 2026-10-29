package com.pict.wallet.dto;

import com.pict.wallet.model.*;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {
    private final Long id;
    private final String initiatorEmail;
    private final String recipientEmail;
    private final BigDecimal amount;
    private final TransactionType type;
    private final TransactionStatus status;
    private final String message;
    private final Instant createdAt;

    public TransactionResponse(WalletTransaction transaction) {
        id = transaction.getId();
        initiatorEmail = transaction.getInitiator().getEmail();
        recipientEmail = transaction.getRecipientEmail();
        amount = transaction.getAmount();
        type = transaction.getType();
        status = transaction.getStatus();
        message = transaction.getMessage();
        createdAt = transaction.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getInitiatorEmail() { return initiatorEmail; }
    public String getRecipientEmail() { return recipientEmail; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
