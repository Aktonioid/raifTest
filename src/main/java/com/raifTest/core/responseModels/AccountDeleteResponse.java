package com.raifTest.core.responseModels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class AccountDeleteResponse {
    private boolean balanceNotEmpty; // баланс счета не нулевой
    private boolean deletionError; // возникла ошибка при удалении
    private boolean accountNotFound; // нет счета
    private boolean notCustomerAccount; // Счет не относится к клиенту
}
