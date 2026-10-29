package com.pict.wallet.dto;

import com.pict.wallet.model.WalletUser;
import java.math.BigDecimal;

// Return only safe account details, never the entity's password hash.
public class WalletResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final BigDecimal balance;

    public WalletResponse(WalletUser user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.balance = user.getBalance();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public BigDecimal getBalance() { return balance; }
}
