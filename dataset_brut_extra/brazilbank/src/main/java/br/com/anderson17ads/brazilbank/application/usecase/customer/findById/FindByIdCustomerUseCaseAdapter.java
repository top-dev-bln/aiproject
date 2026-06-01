package br.com.anderson17ads.brazilbank.application.usecase.customer.findById;

import br.com.anderson17ads.brazilbank.domain.customer.Customer;
import br.com.anderson17ads.brazilbank.domain.customer.CustomerRepository;

import java.util.UUID;

public class FindByIdCustomerUseCaseAdapter implements FindByIdCustomerUseCase {
    CustomerRepository customerRepository;

    public FindByIdCustomerUseCaseAdapter(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer execute(UUID id) {
        return customerRepository.findById(id).orElse(null);
    }
}
