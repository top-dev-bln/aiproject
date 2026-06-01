package br.com.anderson17ads.brazilbank.application.usecase.customer.list;

import br.com.anderson17ads.brazilbank.domain.customer.Customer;

import java.util.List;

public interface ListCustomerUseCase {
    public List<Customer> execute();
}
