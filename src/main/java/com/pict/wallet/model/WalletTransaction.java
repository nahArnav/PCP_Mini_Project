package com.pict.wallet.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The initiating user is always present, including a failed attempt.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private WalletUser initiator;

    // Null for an add-money operation or an unknown recipient.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private WalletUser recipient;

    @Column(length = 120)
    private String recipientEmail;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionStatus status;

    @Column(nullable = false, length = 160)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected WalletTransaction() { }

    public WalletTransaction(WalletUser initiator, WalletUser recipient, String recipientEmail,
                             BigDecimal amount, TransactionType type, TransactionStatus status, String message) {
        this.initiator = initiator;
        this.recipient = recipient;
        this.recipientEmail = recipientEmail;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.message = message;
    }

    public Long getId() { return id; }
    public WalletUser getInitiator() { return initiator; }
    public WalletUser getRecipient() { return recipient; }
    public String getRecipientEmail() { return recipientEmail; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
