package br.com.anderson17ads.brazilbank.domain.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Account {
    private UUID id;
    private String number;
    private BigDecimal balance;
    private UUID customerId;
    private AccountType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Account() {}

    public Account(
        UUID id,
        String number,
        BigDecimal balance,
        UUID customerId,
        AccountType type
    ) {
        this.id = id;
        this.number = number;
        this.balance = balance;
        this.customerId = customerId;
        this.type = type;
    }

    public Account(
        String number,
        BigDecimal balance,
        UUID customerId,
        AccountType type
    ) {
        this.number = number;
        this.balance = balance;
        this.customerId = customerId;
        this.type = type;
    }

    public void create() {
        this.createdAt = LocalDateTime.now();
    }

    public void update() {
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public AccountType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
