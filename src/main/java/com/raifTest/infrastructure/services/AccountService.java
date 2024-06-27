package com.raifTest.infrastructure.services;

import com.raifTest.core.dto.AccountDto;
import com.raifTest.core.enums.AccountType;
import com.raifTest.core.mappers.AccountMapper;
import com.raifTest.core.models.Account;
import com.raifTest.core.models.Customer;
import com.raifTest.core.requestModels.AccountCreationRequestModel;
import com.raifTest.core.responseModels.AccountDeleteResponse;
import com.raifTest.core.responseModels.AccountTransferResponse;
import com.raifTest.core.respositories.IAccountRepo;
import com.raifTest.core.services.IAccountService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AccountService implements IAccountService {

    final private IAccountRepo accountRepo;
    final private AccountMapper mapper;

    public AccountService(IAccountRepo accountRepo){
        this.accountRepo = accountRepo;
        mapper = new AccountMapper();
    }

    @Override
    @Async
    public CompletableFuture<AccountDto> getAccountBySerialNumber(String serial, UUID customerId) {
        return CompletableFuture.supplyAsync(() ->{
            // получаем счет по номеру и переводим в dto
            AccountDto accountDto = mapper.asDto(accountRepo.getAccountBySerial(serial));

            if(accountDto == null){
                return null;
            }

            // Если запрашивает не тот клиент, то возвращаем null для клиента будет выглядеть как 404 notFound
            if(!accountDto.getCustomerId().equals(customerId)){
                return null;
            }

            return accountDto;
        });
    }

    @Override
    @Async
    public CompletableFuture<List<AccountDto>> getAllAccountsByCustomerId(UUID customerId) {
        return CompletableFuture.supplyAsync(() ->{

            List<AccountDto> acctounts = accountRepo.getAllAccountsByCustomerId(customerId).stream()
                    .map(entity -> mapper.asDto(entity)).collect(Collectors.toList());

            return acctounts;
        });
    }

    @Override
    @Async
    // хз как тест для проверки написать, мб вынести метод для генерации номера счета в отдельный метод и проверить его
    public CompletableFuture<Boolean> createAccount(AccountCreationRequestModel creation) {
        return CompletableFuture.supplyAsync(()->{

            // генерация номера счета пользователя
            // Сделал номер счета собираемым и через string builder, чтоб если понадобится можно было сделать создание разных типов счетов
            StringBuilder sb = new StringBuilder();
            sb.append("408");
            sb.append("17");

            switch (creation.getCurrency()){
                case EUR -> sb.append(987);
                case RUB -> sb.append(810);
                case USD -> sb.append(840);
            }

            Random random = new Random();
            sb.append(random.nextInt(1,10));
            int branchNum = random.nextInt(1, 10000);

            for (int i = 0; i < 4 - String.valueOf(branchNum).length(); i++)
            {
                sb.append(0);
            }
            sb.append(branchNum);

            int accountNum = random.nextInt(1, 10000000);

            for (int i = 0; i < 7-String.valueOf(accountNum).length(); i++){
                sb.append(0);
            }
            sb.append(accountNum);

            // номер счета сгенерирован

            Account account = new Account(
                    sb.toString(),
                    new Customer(creation.getCustomerId()),
                    0,
                    new Date(),
                    creation.getCurrency()
            );

            return accountRepo.createAccount(account);
        });
    }

    @Override
    @Async
    // Пока на счету есть хотя бы 0.01 единица денег, то нельзя удалить счет
    public CompletableFuture<AccountDeleteResponse> deleteAccount(String serial, UUID customerId) {

        return CompletableFuture.supplyAsync(()->{
            AccountDeleteResponse response = new AccountDeleteResponse();

            Account account = accountRepo.getAccountBySerial(serial);

            // проверка есть ли вообще такой счет
            if(account== null){
                response.setAccountNotFound(true);
                return response;
            }

            // проверка на то принадлежит ли счет клиенту
            if(!account.getCustomer().getId().equals(customerId)){
                response.setNotCustomerAccount(true);
                return response;
            }

            // проверка на то есть ли на счету деньги
            if(account.getBalance() >= 0.01){
                response.setBalanceNotEmpty(true);
                return response;
            }

            // Удаление и проверка на то успешно ли удалилось
            if(!accountRepo.deleteAccount(serial)){
                response.setDeletionError(true);
            }

            return response;
        });
    }

    @Override
    @Async
    public CompletableFuture<List<AccountDto>> getAccountsByCreationDateAndCustomer(UUID customerId, Date date) {
        return CompletableFuture.supplyAsync(() -> {

            List<AccountDto> accounts = accountRepo.getAccountsByCreationDateAndCustomer(customerId, date)
                    .stream()
                    .map(account -> mapper.asDto(account))
                    .collect(Collectors.toList());

            return accounts;
        });
    }

    @Override
    @Async
    public CompletableFuture<List<AccountDto>> getAccountsByBalanceAndCustomer(UUID customerId, double balance) {
        return CompletableFuture.supplyAsync(() ->{

            List<AccountDto> accounts = accountRepo.getAccountsByBalanceAndCustomer(customerId, balance)
                    .stream()
                    .map(model -> mapper.asDto(model))
                    .collect(Collectors.toList());

            return accounts;
        });
    }

    @Override
    @Async
    public CompletableFuture<List<AccountDto>> getAccountsByCustomerAndAccountType(UUID customerId, AccountType type) {
        return CompletableFuture.supplyAsync(()->{

            List<AccountDto> accounts =accountRepo.getAccountsByCustomerAndAccountType(customerId, type)
                    .stream()
                    .map(account -> mapper.asDto(account))
                    .collect(Collectors.toList());

            return accounts;
        });
    }

    @Override
    @Async
    public CompletableFuture<AccountTransferResponse> transferFromAccountToAccount(String serialFrom,
                                                                                   String serialTo,
                                                                                   double amount,
                                                                                   UUID senderId) {

        CompletableFuture<AccountTransferResponse> response = CompletableFuture.supplyAsync(() ->{

            AccountTransferResponse accountResponse = new AccountTransferResponse();

            Account accountFrom = accountRepo.getAccountBySerial(serialFrom);

            double sum = round(amount, 2);// сумма перевода

            // счета нет -> ошибка
            if(accountFrom == null){
                accountResponse.setAccountNotExhist(true);
                return accountResponse;
            }

            // Дополнительная проверка на то, точно счет с которого отправляются деньги принадлежит данному клиенту
            if(!accountFrom.getCustomer().getId().equals(senderId)){
                accountResponse.setNotCustomersAccount(true);
                return accountResponse;
            }

            //проверка на то хватает ли денег для отправки
            if(!checkIfEnoughMoney(accountFrom, sum)){
                accountResponse.setNotEnoughMoney(true);
                return accountResponse;
            }

            Account accountTo = accountRepo.getAccountBySerial(serialTo);
            if (accountTo == null){
                accountResponse.setAccountNotExhist(true);
                return accountResponse;
            }

            // Перевод из одной валюты в другую
            double amountInSerialToCur = 0;

            // данные по стоимости валют брал 25.06
            switch (accountFrom.getType()){
                case EUR -> {
                    switch (accountTo.getType()){
                        case RUB -> amountInSerialToCur = sum*93.75;
                        case USD -> amountInSerialToCur= sum*1.07;
                        case EUR -> amountInSerialToCur = sum;
                    }
                }
                case RUB -> {
                    switch (accountTo.getType()){
                        case RUB -> amountInSerialToCur = sum;
                        case USD -> amountInSerialToCur = sum*0.01;
                        case EUR -> amountInSerialToCur = sum*0.01;
                    }
                }

                case USD -> {
                    switch (accountTo.getType()){
                        case RUB -> amountInSerialToCur = sum*87.5;
                        case USD -> amountInSerialToCur = sum;
                        case EUR -> amountInSerialToCur = sum*0.93;
                    }
                }
            }

            amountInSerialToCur = round(amountInSerialToCur, 2);

            if(amountInSerialToCur < 0.01 || amount < 0.01){
                accountResponse.setImpossibleTransfer(true);
                return accountResponse;
            }

            if(!accountRepo.transferFromAccountToAccount(serialFrom, serialTo, sum, amountInSerialToCur)){
                accountResponse.setTransferError(true);
            }

            return accountResponse;
        }
        );

        return response;
    }

    @Override
    @Async
    public CompletableFuture<AccountTransferResponse> refilAccountBalance(String serialNumber, double amount, UUID customerId) {
        return CompletableFuture.supplyAsync(() ->{

            AccountTransferResponse accountResponse = new AccountTransferResponse();

            Account account = accountRepo.getAccountBySerial(serialNumber);

            // проверка на то, а есть ли такой счет
            if(account == null){
                accountResponse.setAccountNotExhist(true);
                return accountResponse;
            }

            // проверка на то, принадлежит ли счет пользователю
            if(!account.getCustomer().getId().equals(customerId)){
                accountResponse.setNotCustomersAccount(true);
                return accountResponse;
            }

            // Проведение пополнения, и проверка на то нет ли ошибок во время пополнения
            if(!accountRepo.refilAccountBalance(serialNumber, round(amount, 2))){
                accountResponse.setTransferError(true);
                return accountResponse;
            }

            return accountResponse;
        });
    }

    @Override
    @Async
    public CompletableFuture<AccountTransferResponse> withdrawAccountBalance(String serialNumber, double amount, UUID customerId) {
        return CompletableFuture.supplyAsync(() ->{
            AccountTransferResponse response = new AccountTransferResponse();

            Account account = accountRepo.getAccountBySerial(serialNumber);

            // сумма снимаемых денег
            double sum = round(amount,2);

            // проверка на существование счета
            if(account == null){
                response.setAccountNotExhist(true);
                return response;
            }

            // проверка на принадлежность счета к пользователю

            if(!account.getCustomer().getId().equals(customerId)){
                response.setNotCustomersAccount(true);
                return response;
            }

            // проверка на то хватает ли денег на счете
            if(!checkIfEnoughMoney(account, sum)){
                response.setNotEnoughMoney(true);
                return  response;
            }

            // Проведение операции и проверка на успешно ли она прошла
            if(!accountRepo.withdrawAccountBalance(serialNumber, sum)){
                response.setTransferError(true);
            }

            return response;
        });
    }

    //ture когда денег хватает средств, false когда не хватает
    private boolean checkIfEnoughMoney(Account account, double ammount){
        return account.getBalance() >= ammount;
    }

    // функция для округления до нужного числа знаков
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}

