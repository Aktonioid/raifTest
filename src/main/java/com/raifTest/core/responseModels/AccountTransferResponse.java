package com.raifTest.core.responseModels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class AccountTransferResponse {
    private boolean notEnoughMoney;
    private boolean accountNotExhist;
    private boolean transferError;
    private boolean notCustomersAccount;
    private boolean impossibleTransfer; // Если пытаются перевести менее чем 0.01 условной единицы
}
