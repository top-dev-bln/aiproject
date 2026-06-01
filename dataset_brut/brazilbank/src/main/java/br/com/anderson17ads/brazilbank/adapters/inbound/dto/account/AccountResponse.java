package br.com.anderson17ads.brazilbank.adapters.inbound.dto.account;

import br.com.anderson17ads.brazilbank.domain.account.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountResponse {
    private UUID id;
    private String number;
    private BigDecimal balance;

    @JsonProperty("customer_id")
    private UUID customerId;
    private AccountType type;
}
