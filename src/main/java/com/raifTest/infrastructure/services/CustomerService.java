package com.raifTest.infrastructure.services;

import com.raifTest.core.dto.CustomerDto;
import com.raifTest.core.mappers.CustomerMapper;
import com.raifTest.core.models.Customer;
import com.raifTest.core.responseModels.CustomerCreationResponse;
import com.raifTest.core.responseModels.CustomerDeletionResponse;
import com.raifTest.core.respositories.ICustomerRepo;
import com.raifTest.core.services.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class CustomerService implements ICustomerService {

    private final ICustomerRepo customerRepo;
    private final CustomerMapper mapper;
    private final PasswordEncoder encoder;

    @Autowired
    public CustomerService(ICustomerRepo customerRepo, PasswordEncoder encoder){
        this.customerRepo = customerRepo;
        this.mapper =new CustomerMapper();
        this.encoder = encoder;
    }

    @Override
    @Async
    public CompletableFuture<CustomerDto> getCustomerById(UUID id) {
        return CompletableFuture.supplyAsync(() ->{
            return mapper.asDto(customerRepo.getCustomerById(id));
        });
    }

    @Override
    @Async
    public CompletableFuture<CustomerDto> getCustomerByUsername(String username) {
        return CompletableFuture.supplyAsync(()->{
            return mapper.asDto(customerRepo.getCustomerByUsername(username));
        });
    }

    @Override
    @Async
    public CompletableFuture<CustomerCreationResponse> createCustomer(CustomerDto customerDto) {

        // проверка на то, существует ли такой пароль
        CompletableFuture<Boolean> response = CompletableFuture.supplyAsync(() ->{
            return customerRepo.isCustomerExistsByUsername(customerDto.getUsername());
        });

        return response.thenApply(result ->{
            CustomerCreationResponse creationResponse = new CustomerCreationResponse();

            // если пользователь с таким именем существует
            if(result){
                creationResponse.setUsernameExist(true);
                return creationResponse;
            }

            // Шифровка пароля
            customerDto.setPassword(encoder.encode(customerDto.getPassword()));

            customerDto.setId(UUID.randomUUID());
            // Создание пользователя и проверка на то, сохранилось ли в бд
            if(!customerRepo.createCustomer(mapper.asEntity(customerDto))){
                creationResponse.setCreationError(true);
            }

            return creationResponse;
        });

    }

    @Override
    @Async
    public CompletableFuture<Boolean> updateCustomerPassword(String password, UUID customerId) {
        CompletableFuture<Customer> response = CompletableFuture.supplyAsync(() ->{
            return customerRepo.getCustomerById(customerId);
        });
        return response.thenApply(customer ->{
            if(customer == null){
                return false;
            }

            customer.setPassword(encoder.encode(password));

            return customerRepo.updateCustomer(customer);
        });
    }

    @Override
    @Async
    public CompletableFuture<Boolean> updateCustomerName(String name, UUID customerID) {
        CompletableFuture<Customer> response = CompletableFuture.supplyAsync(() ->{
            return customerRepo.getCustomerById(customerID);
        });
        return response.thenApply(customer ->{
            if(customer == null){
                return  false;
            }

            customer.setName(name);

            return customerRepo.updateCustomer(customer);
        });
    }

    @Override
    @Async
    public CompletableFuture<Boolean> isCustomerExistByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->{
            return customerRepo.isCustomerExistsByUsername(username);
        });
    }

    @Override
    @Async
    public CompletableFuture<CustomerDeletionResponse> deleteCustomerById(UUID id) {

        return CompletableFuture.supplyAsync(() -> {
            CustomerDeletionResponse response = new CustomerDeletionResponse();

            Customer customer = customerRepo.getCustomerById(id);

            // проверка на то, есть ли такой клиент
            if(customer == null){
                response.setUserNotFound(true);
                return  response;
            }

            // Проверка на то, закрыты ли счета у клиента
            if(!customer.getAccounts().isEmpty()){
                response.setCustomerHasAccounts(true);
                return response;
            }

            // удаление пользователя и проверка на то нет ли ошибки при удалении
            if(!customerRepo.deleteCustomerById(id)){
                response.setDeletiionError(true);
            }

            return response;
        });
    }

    @Override
    public CompletableFuture<Boolean> singIn(String username, String password) {

        return CompletableFuture.supplyAsync(() ->{
            Customer customer = customerRepo.getCustomerByUsername(username);

            if(customer == null) return  false;

            if(!encoder.matches(password, customer.getPassword())) return false;

            return true;
        });
    }
}
