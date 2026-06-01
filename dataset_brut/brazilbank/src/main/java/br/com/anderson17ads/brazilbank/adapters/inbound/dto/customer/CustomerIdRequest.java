package br.com.anderson17ads.brazilbank.adapters.inbound.dto.customer;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class CustomerIdRequest {
    @NotBlank(message = "UUID cannot be null")
    private String id;

    public CustomerIdRequest(String id) {
        this.id = id;
    }

    public UUID toUUID() {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Customer UUID Invalid");
        }
    }
}
