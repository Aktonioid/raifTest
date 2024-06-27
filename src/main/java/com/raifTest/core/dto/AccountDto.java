package com.raifTest.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raifTest.core.enums.AccountType;
import com.raifTest.core.models.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AccountDto {

    private String serialNumber;

    private UUID customerId;

    private double balance;

    private Date creationDate;

    private AccountType type;
}
