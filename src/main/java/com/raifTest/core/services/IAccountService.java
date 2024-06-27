package com.raifTest.core.services;

import com.raifTest.core.dto.AccountDto;
import com.raifTest.core.enums.AccountType;
import com.raifTest.core.requestModels.AccountCreationRequestModel;
import com.raifTest.core.responseModels.AccountDeleteResponse;
import com.raifTest.core.responseModels.AccountTransferResponse;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IAccountService {
    // TODO Мб добавить javadoc
    public CompletableFuture<AccountDto> getAccountBySerialNumber(String serial, UUID customerId);
    public CompletableFuture<List<AccountDto>> getAllAccountsByCustomerId(UUID customerId);
    public CompletableFuture<List<AccountDto>> getAccountsByCreationDateAndCustomer(UUID customerId, Date date);
    public CompletableFuture<List<AccountDto>> getAccountsByBalanceAndCustomer(UUID customerId, double balance);
    public CompletableFuture<List<AccountDto>> getAccountsByCustomerAndAccountType(UUID csutomerId, AccountType type);

    public CompletableFuture<Boolean> createAccount(AccountCreationRequestModel creation); //
    //удалить счет можно только если на нем нет средств
    public CompletableFuture<AccountDeleteResponse> deleteAccount(String serial, UUID customerId);
    public CompletableFuture<AccountTransferResponse> transferFromAccountToAccount(String serialFrom,
                                                                                   String serialTo,
                                                                                   double amount,
                                                                                   UUID senderId);
    public CompletableFuture<AccountTransferResponse> refilAccountBalance(String serialNumber, double amount, UUID customerId);
    public CompletableFuture<AccountTransferResponse> withdrawAccountBalance(String serialNumber, double amount, UUID customerId);
}
