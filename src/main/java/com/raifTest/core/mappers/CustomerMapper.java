package com.raifTest.core.mappers;

import com.raifTest.core.dto.CustomerDto;
import com.raifTest.core.models.Customer;

public class CustomerMapper {
    public Customer asEntity(CustomerDto dto){

        if(dto == null){
            return null;
        }

        return new Customer(dto.getId(),
                dto.getName(),
                dto.getUsername(),
                dto.getPassword());
    }

    public CustomerDto asDto(Customer customer){

        if(customer == null) return  null;

        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getUsername(),
                customer.getPassword()
        );
    }
}
