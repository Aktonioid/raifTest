package com.raifTest.core.respositories;

import com.raifTest.core.models.Customer;

import java.util.UUID;

public interface ICustomerRepo {

    public Boolean createCustomer(Customer customer);
    public Boolean updateCustomer(Customer customer);
    public Customer getCustomerById(UUID id);
    public Customer getCustomerByUsername(String username);
    //true если клиент есть, false, если клиента нет
    public boolean isCustomerExistsByUsername(String username);
    public Boolean deleteCustomerById(UUID userId);
}
