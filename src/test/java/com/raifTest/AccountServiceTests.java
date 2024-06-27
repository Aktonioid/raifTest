package com.raifTest;

import com.raifTest.core.enums.AccountType;
import com.raifTest.core.models.Account;
import com.raifTest.core.models.Customer;
import com.raifTest.core.requestModels.AccountCreationRequestModel;
import com.raifTest.core.responseModels.AccountDeleteResponse;
import com.raifTest.core.responseModels.AccountTransferResponse;
import com.raifTest.core.respositories.IAccountRepo;
import com.raifTest.core.services.IAccountService;
import com.raifTest.infrastructure.services.AccountService;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {

    @Mock
    IAccountRepo accountRepo;
    @InjectMocks
    AccountService service;

    //Закидываем создание счета с типом счета usd
    @Test
    void createAccount_USD_true() throws ExecutionException, InterruptedException {

        AccountCreationRequestModel requestModel = new AccountCreationRequestModel();

        requestModel.setCurrency(AccountType.USD);
        requestModel.setCustomerId(UUID.randomUUID());

        when(accountRepo.createAccount(any(Account.class))).thenAnswer( invocation ->{

            Account account = invocation.getArgument(0);
            // проверка на то, правильно ли сгенерился номер
            if(account.getSerialNumber().length() != 20){
                return false;
            }

            // проверка на то правильную ли валюту поставило в string
            if(!account.getSerialNumber().startsWith("840",5)){
                return false;
            }

            return true;
                }
        );

        assertEquals(CompletableFuture.completedFuture(true).get(), service.createAccount(requestModel).get());
    }

    //Закидываем создание счета с типом счета eur
    @Test
    void createAccount_EUR_true() throws ExecutionException, InterruptedException {
        AccountCreationRequestModel requestModel = new AccountCreationRequestModel();

        requestModel.setCurrency(AccountType.EUR);
        requestModel.setCustomerId(UUID.randomUUID());

        when(accountRepo.createAccount(any(Account.class))).thenAnswer( invocation ->{

                    Account account = invocation.getArgument(0);
                    // проверка на то, правильно ли сгенерился номер
                    if(account.getSerialNumber().length() != 20){
                        return false;
                    }

                    // проверка на то правильную ли валюту поставило в string
                    if(!account.getSerialNumber().startsWith("987",5)){
                        return false;
                    }

                    return true;
                }
        );

        assertEquals(CompletableFuture.completedFuture(true).get(), service.createAccount(requestModel).get());
    }

    //Закидываем создание счета с типом счета rub
    @Test
    void createAccount_RUB_true() throws ExecutionException, InterruptedException {
        AccountCreationRequestModel requestModel = new AccountCreationRequestModel();

        requestModel.setCurrency(AccountType.RUB);
        requestModel.setCustomerId(UUID.randomUUID());

        when(accountRepo.createAccount(any(Account.class))).thenAnswer( invocation ->{

                    Account account = invocation.getArgument(0);
                    // проверка на то, правильно ли сгенерился номер
                    if(account.getSerialNumber().length() != 20){
                        return false;
                    }

                    // проверка на то правильную ли валюту поставило в string
                    if(!account.getSerialNumber().startsWith("810",5)){
                        return false;
                    }

                    return true;
                }
        );

        assertEquals(CompletableFuture.completedFuture(true).get(), service.createAccount(requestModel).get());
    }

    @Test
    void deleteAccount_AccountNotFound() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";

        when(accountRepo.getAccountBySerial(serial)).thenReturn(null);

        AccountDeleteResponse expectedResponse = new AccountDeleteResponse();

        expectedResponse.setAccountNotFound(true);

        assertEquals(expectedResponse, service.deleteAccount(serial, customerId).get());
    }

    @Test
    void deleteAccount_notCustomerAccount() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(UUID.randomUUID()),
                0,
                new Date(),
                AccountType.EUR
        ));

        AccountDeleteResponse expectedResponse = new AccountDeleteResponse();
        expectedResponse.setNotCustomerAccount(true);

        assertEquals(expectedResponse, service.deleteAccount(serial, customerId).get());
    }

    @Test
    void deleteAccount_BalanceNotEmpty() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(customerId),
                1,
                new Date(),
                AccountType.EUR
        ));

        AccountDeleteResponse expectedResponse = new AccountDeleteResponse();

        expectedResponse.setBalanceNotEmpty(true);

        assertEquals(expectedResponse, service.deleteAccount(serial,customerId).get());
    }

    @Test
    void deleteAccount_DeletionSuccessful() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(customerId),
                0,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.deleteAccount(serial)).thenReturn(true);

        AccountDeleteResponse expectedResponse = new AccountDeleteResponse();

        assertEquals(expectedResponse, service.deleteAccount(serial, customerId).get());
    }

    @Test
    void deleteAccount_DeletionError() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(customerId),
                0,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.deleteAccount(serial)).thenReturn(false);

        AccountDeleteResponse expectedResponse = new AccountDeleteResponse();

        expectedResponse.setDeletionError(true);

        assertEquals(expectedResponse, service.deleteAccount(serial, customerId).get());
    }

    //transferFromAccountToAccount
    @Test
    void transferFromAccountToAccount_AccountFromNotFound() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serialFrom = "40817987258048214502";//usd acc
        String serialTo = "40817810176854866777"; //rub acc

        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setAccountNotExhist(true);

        when(accountRepo.getAccountBySerial(serialFrom)).thenReturn(null);

        assertEquals(expectedResponse, service.transferFromAccountToAccount(serialFrom,serialTo, 1, customerId).get());
    }

    @Test
    void transferFromAccountToAccount_AccountToNotFound() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serialFrom = "40817987258048214502";//usd acc
        String serialTo = "40817810176854866777"; //rub acc
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setAccountNotExhist(true);

        when(accountRepo.getAccountBySerial(serialFrom)).thenReturn(new Account(
                serialFrom,
                new Customer(customerId),
                1000,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.getAccountBySerial(serialTo)).thenReturn(null);

        assertEquals(expectedResponse, service.transferFromAccountToAccount(serialFrom,serialTo, 1, customerId).get());
    }

    @Test
    void transferFromAccountToAccount_NotEnoughMoney() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serialFrom = "40817987258048214502";//usd acc
        String serialTo = "40817810176854866777"; //rub acc
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setNotEnoughMoney(true);

        when(accountRepo.getAccountBySerial(serialFrom)).thenReturn(new Account(
                serialFrom,
                new Customer(customerId),
                0,
                new Date(),
                AccountType.EUR
        ));

        assertEquals(expectedResponse, service.transferFromAccountToAccount(serialFrom,serialTo, 1, customerId).get());
    }

    @Test
    void transferFromAccountToAccount_InvalidSender() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serialFrom = "40817987258048214502";//usd acc
        String serialTo = "40817810176854866777"; //rub acc
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setNotCustomersAccount(true);

        when(accountRepo.getAccountBySerial(serialFrom)).thenReturn(new Account(
                serialFrom,
                new Customer(UUID.randomUUID()),
                10,
                new Date(),
                AccountType.EUR
        ));

        assertEquals(expectedResponse, service.transferFromAccountToAccount(serialFrom,serialTo, 1, customerId).get());
    }

    @Test
    void transferFromAccountToAccount_InvalidTransferCurrency() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serialFrom = "40817987258048214502";//usd acc
        String serialTo = "40817810176854866777"; //rub acc
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setImpossibleTransfer(true);

        when(accountRepo.getAccountBySerial(serialFrom)).thenReturn(new Account(
                serialFrom,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.getAccountBySerial(serialTo)).thenReturn(new Account(
                serialFrom,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));


        assertEquals(expectedResponse, service.transferFromAccountToAccount(serialFrom,serialTo, 0.001, customerId).get());
    }

    @Test
    void transferFromAccountToAccount_SuccessfulTransfer() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serialFrom = "40817987258048214502";//usd acc
        String serialTo = "40817810176854866777"; //rub acc
        double amount = 1;
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
//        expectedResponse.setImpossibleTransfer(true);

        when(accountRepo.getAccountBySerial(serialFrom)).thenReturn(new Account(
                serialFrom,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.getAccountBySerial(serialTo)).thenReturn(new Account(
                serialTo,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.transferFromAccountToAccount(eq(serialFrom),eq(serialTo),eq(amount), anyDouble())).thenReturn(true);

        assertEquals(expectedResponse, service.transferFromAccountToAccount(serialFrom,serialTo, amount, customerId).get());
    }

    @Test
    void transferFromAccountToAccount_TransferError() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serialFrom = "40817987258048214502";//usd acc
        String serialTo = "40817810176854866777"; //rub acc
        double amount = 1;
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setTransferError(true);

        when(accountRepo.getAccountBySerial(serialFrom)).thenReturn(new Account(
                serialFrom,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.getAccountBySerial(serialTo)).thenReturn(new Account(
                serialTo,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.transferFromAccountToAccount(eq(serialFrom),eq(serialTo),eq(amount), anyDouble())).thenReturn(false);

        assertEquals(expectedResponse, service.transferFromAccountToAccount(serialFrom,serialTo, amount, customerId).get());
    }


    //withdrawAccountBalance tests
    @Test
    void withdrawAccountBalance_AccountNotFound() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        double withdrawAmount = 1000;
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setAccountNotExhist(true);

        when(accountRepo.getAccountBySerial(serial)).thenReturn(null);

        assertEquals(expectedResponse, service.withdrawAccountBalance(serial,withdrawAmount,customerId).get());

    }


    @Test
    void withdrawAccountBalance_NotEnoughMoney() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        double withdrawAmount = 1000;
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setNotEnoughMoney(true);

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        assertEquals(expectedResponse, service.withdrawAccountBalance(serial,withdrawAmount,customerId).get());

    }

    @Test
    void withdrawAccountBalance_InvalidSender() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        double withdrawAmount = 1000;
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setNotCustomersAccount(true);

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(UUID.randomUUID()),
                10,
                new Date(),
                AccountType.EUR
        ));

        assertEquals(expectedResponse, service.withdrawAccountBalance(serial,withdrawAmount,customerId).get());

    }

    @Test
    void withdrawAccountBalance_SuccessfulWithdraw() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        double withdrawAmount = 10;
        AccountTransferResponse expectedResponse = new AccountTransferResponse();

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.withdrawAccountBalance(serial, withdrawAmount)).thenReturn(true);

        assertEquals(expectedResponse, service.withdrawAccountBalance(serial,withdrawAmount,customerId).get());

    }

    @Test
    void withdrawAccountBalance_WithdrawError() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        double withdrawAmount = 10;
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setTransferError(true);

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.withdrawAccountBalance(serial,withdrawAmount)).thenReturn(false);

        assertEquals(expectedResponse, service.withdrawAccountBalance(serial,withdrawAmount,customerId).get());
    }

    //refilAccountBalance Tests

    @Test
    void refilAccountBalance_AccountNotFound() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setAccountNotExhist(true);

        when(accountRepo.getAccountBySerial(serial)).thenReturn(null);
        assertEquals(expectedResponse, service.refilAccountBalance(serial, 100,customerId).get());

    }


    @Test
    void refilAccountBalance_InvalidSender() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setNotCustomersAccount(true);

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(UUID.randomUUID()),
                10,
                new Date(),
                AccountType.EUR
        ));
        assertEquals(expectedResponse, service.refilAccountBalance(serial, 100,customerId).get());

    }


    @Test
    void refilAccountBalance_SuccessfulTransfer() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        AccountTransferResponse expectedResponse = new AccountTransferResponse();

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.refilAccountBalance(eq(serial), anyDouble())).thenReturn(true);
        assertEquals(expectedResponse, service.refilAccountBalance(serial, 100,customerId).get());

    }

    @Test
    void refilAccountBalance_TransferError() throws ExecutionException, InterruptedException {
        UUID customerId = UUID.randomUUID();
        String serial = "40817987258048214502";
        AccountTransferResponse expectedResponse = new AccountTransferResponse();
        expectedResponse.setTransferError(true);

        when(accountRepo.getAccountBySerial(serial)).thenReturn(new Account(
                serial,
                new Customer(customerId),
                10,
                new Date(),
                AccountType.EUR
        ));

        when(accountRepo.refilAccountBalance(eq(serial), anyDouble())).thenReturn(false);
        assertEquals(expectedResponse, service.refilAccountBalance(serial, 100,customerId).get());

    }
}
