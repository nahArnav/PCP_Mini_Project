package com.pict.wallet.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AddMoneyRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least INR 0.01")
    @DecimalMax(value = "100000.00", message = "Amount cannot exceed INR 100000")
    @Digits(integer = 6, fraction = 2, message = "Use at most two decimal places")
    private BigDecimal amount;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
