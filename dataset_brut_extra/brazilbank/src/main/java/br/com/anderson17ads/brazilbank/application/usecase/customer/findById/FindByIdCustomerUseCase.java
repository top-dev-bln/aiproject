package br.com.anderson17ads.brazilbank.application.usecase.customer.findById;

import br.com.anderson17ads.brazilbank.domain.customer.Customer;
import java.util.UUID;

public interface FindByIdCustomerUseCase {
    public Customer execute(UUID id);
}
