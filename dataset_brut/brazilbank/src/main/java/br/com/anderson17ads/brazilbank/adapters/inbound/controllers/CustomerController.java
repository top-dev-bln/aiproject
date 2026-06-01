package br.com.anderson17ads.brazilbank.adapters.inbound.controllers;

import br.com.anderson17ads.brazilbank.adapters.inbound.dto.customer.CustomerIdRequest;
import br.com.anderson17ads.brazilbank.adapters.inbound.dto.customer.CustomerRequest;
import br.com.anderson17ads.brazilbank.adapters.inbound.dto.customer.CustomerResponse;
import br.com.anderson17ads.brazilbank.adapters.inbound.mapper.InboundCustomerMapper;
import br.com.anderson17ads.brazilbank.adapters.inbound.paths.ApiPaths;
import br.com.anderson17ads.brazilbank.application.usecase.customer.create.CreateCustomerUseCase;
import br.com.anderson17ads.brazilbank.application.usecase.customer.findById.FindByIdCustomerUseCase;
import br.com.anderson17ads.brazilbank.application.usecase.customer.list.ListCustomerUseCase;
import br.com.anderson17ads.brazilbank.domain.customer.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RequestMapping(ApiPaths.CUSTOMER)
@RestController
public class CustomerController {
    private final CreateCustomerUseCase createCustomerUseCase;
    private final ListCustomerUseCase listCustomerUseCase;
    private final FindByIdCustomerUseCase findByIdCustomerUseCase;

    public CustomerController(
            CreateCustomerUseCase createCustomerUseCase,
            ListCustomerUseCase listCustomerUseCase,
            FindByIdCustomerUseCase findByIdCustomerUseCase
    ) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.listCustomerUseCase = listCustomerUseCase;
        this.findByIdCustomerUseCase = findByIdCustomerUseCase;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer created = createCustomerUseCase.execute(
                InboundCustomerMapper.toCommand(request)
        );

        URI location = URI.create(String.format("%s/%s",
                ApiPaths.CUSTOMER,
                created.getId()
        ));

        return ResponseEntity
                .created(location)
                .body(InboundCustomerMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> listAll() {
        List<CustomerResponse> response = InboundCustomerMapper.toResponseList(
                listCustomerUseCase.execute()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@Valid @PathVariable String id) {
        CustomerIdRequest request = new CustomerIdRequest(id);
        CustomerResponse response = InboundCustomerMapper.toResponse(
                findByIdCustomerUseCase.execute(request.toUUID())
        );

        return ResponseEntity.ok(response);
    }
}
