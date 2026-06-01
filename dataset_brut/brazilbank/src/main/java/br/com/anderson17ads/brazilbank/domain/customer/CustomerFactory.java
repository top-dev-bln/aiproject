package br.com.anderson17ads.brazilbank.domain.customer;

import java.time.LocalDate;

public class CustomerFactory {
    public Customer create(
            String name,
            String email,
            String document,
            String phone,
            LocalDate birthDate
    ) {
        Customer customer = new Customer(name, email, document, phone, birthDate);
        customer.create();
        return customer;
    }
}
