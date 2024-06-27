package com.raifTest.core.requestModels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raifTest.core.enums.AccountType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AccountCreationRequestModel {

    // customer, который создает
    @JsonIgnore
    // добавляется в модель в контроллере
    private UUID customerId;

    // Начало описания номера счета

    // Так как вариант для физ. лица, то открывает счет всегда человек, по этому начало счет всегда будет 408 17
    // Валюта в которой открыт счет
    private AccountType currency;


    //пока что филлиал и номер счета будут генериться случайно, мб придумаю как иначе сделать, тип в бд номер филлиала закинуть и
    // потом прост получать последний номер который там был, но хз
//    //4-х значный номер филлиала банка
//    @Pattern(regexp = "\\d\\d\\d\\d")
//    private String bankBranch;
//    //Порядковый номер счета(7)
//    @Max(9999999)
//    private int serialNumber;
    // окончание описания номера счета
}
