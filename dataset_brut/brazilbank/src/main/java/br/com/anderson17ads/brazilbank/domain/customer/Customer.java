package br.com.anderson17ads.brazilbank.domain.customer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Customer {
    private UUID id;
    private String name;
    private String email;
    private String document;
    private String phone;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Customer() {}

    public Customer(
            UUID id,
            String name,
            String email,
            String document,
            String phone,
            LocalDate birthDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.document = document;
        this.phone = phone;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public Customer(
            String name,
            String email,
            String document,
            String phone,
            LocalDate birthDate
    ) {
        this.name = name;
        this.email = email;
        this.document = document;
        this.phone = phone;
        this.birthDate = birthDate;
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

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDocument() {
        return document;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
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
