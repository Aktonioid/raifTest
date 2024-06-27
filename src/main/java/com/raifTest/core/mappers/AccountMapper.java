package com.raifTest.core.mappers;

import com.raifTest.core.dto.AccountDto;
import com.raifTest.core.models.Account;
import com.raifTest.core.models.Customer;

public class AccountMapper {

    public Account asEntity(AccountDto dto){

        if(dto == null){
            return null;
        }

        return new Account(
                dto.getSerialNumber(),
                new Customer(dto.getCustomerId()),
                dto.getBalance(),
                dto.getCreationDate(),
                dto.getType()
        );
    }
    public AccountDto asDto(Account account){

        if(account == null){
            return null;
        }
        return new AccountDto(
                account.getSerialNumber(),
                account.getCustomer().getId(),
                account.getBalance(),
                account.getCreationDate(),
                account.getType()
        );
    }
}
