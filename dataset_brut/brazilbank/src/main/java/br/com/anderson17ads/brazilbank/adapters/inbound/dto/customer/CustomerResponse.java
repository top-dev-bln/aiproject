package br.com.anderson17ads.brazilbank.adapters.inbound.dto.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerResponse {
    private UUID id;
    private String name;
    private String email;
    private String document;
    private String phone;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-dd-MM HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-dd-MM HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonProperty("deleted_at")
    @JsonFormat(pattern = "yyyy-dd-MM HH:mm:ss")
    private LocalDateTime deletedAt;
}
