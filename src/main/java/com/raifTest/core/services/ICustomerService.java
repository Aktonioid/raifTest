package com.raifTest.core.services;

import com.raifTest.core.dto.CustomerDto;
import com.raifTest.core.responseModels.CustomerCreationResponse;
import com.raifTest.core.responseModels.CustomerDeletionResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ICustomerService {
    public CompletableFuture<CustomerDto> getCustomerById(UUID id);
    public CompletableFuture<CustomerDto> getCustomerByUsername(String username);
    public CompletableFuture<CustomerCreationResponse> createCustomer(CustomerDto customerDto);
    public CompletableFuture<Boolean> updateCustomerPassword(String password, UUID customerId);
    public CompletableFuture<Boolean> updateCustomerName(String name, UUID customerID);
    public CompletableFuture<Boolean> isCustomerExistByUsername(String username);
    public CompletableFuture<CustomerDeletionResponse> deleteCustomerById(UUID id);
    public CompletableFuture<Boolean> singIn(String username, String password);
}
