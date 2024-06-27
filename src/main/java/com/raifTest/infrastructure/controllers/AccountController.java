package com.raifTest.infrastructure.controllers;

import com.raifTest.core.dto.AccountDto;
import com.raifTest.core.enums.AccountType;
import com.raifTest.core.requestModels.AccountCreationRequestModel;
import com.raifTest.core.responseModels.AccountDeleteResponse;
import com.raifTest.core.responseModels.AccountTransferResponse;
import com.raifTest.core.services.IAccountService;
import com.raifTest.core.services.IJwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/accounts")
// Если прикрутить авторизацию, то надо будет переписать контроллер
public class AccountController {

    private IAccountService accountService;
    private final IJwtService jwtService;

    @Autowired
    public AccountController (IAccountService accountService, IJwtService jwtService){
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    //TODO переписать контроллер на jwt

    @GetMapping("/{serial}")
    public ResponseEntity<AccountDto> getAccountBySerialNumber(@PathVariable String serial,
                                                                                  @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {
            Cookie accessCookie = Arrays
                    .stream(request.getCookies())
                    .filter(c -> c.getName().equals("access"))
                    .findFirst().get();

            if(accessCookie == null){
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));

            AccountDto result;
            try {
                result = accountService.getAccountBySerialNumber(serial, customerId).get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            if ( result ==null){
                return  ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(result);

    }

    @GetMapping("/")
    public ResponseEntity<List<AccountDto>> getAccountsByCustomerID(@NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {

        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));

        CompletableFuture<List<AccountDto>> accounts = accountService.getAllAccountsByCustomerId(customerId);

        return ResponseEntity.ok(accounts.get());
    }

    @GetMapping("/dates")
    public  ResponseEntity<List<AccountDto>> getAccountsByCreationDateAndCustomerId(@RequestParam Long creationDate,
                                                                                    @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {
        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));

        CompletableFuture<List<AccountDto>> accounts = accountService.getAccountsByCreationDateAndCustomer(customerId,new Date(creationDate));
        return ResponseEntity.ok(accounts.get());
    }

    @GetMapping("/balance")
    public  ResponseEntity<List<AccountDto>> getAccountsByBalanceAndCustomerId(@RequestParam double balance,
                                                                               @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {
        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));
        CompletableFuture<List<AccountDto>> accounts = accountService.getAccountsByBalanceAndCustomer(customerId,balance);
        return ResponseEntity.ok(accounts.get());
    }

    @GetMapping("/types")
    public  ResponseEntity<List<AccountDto>> getAccountsByAccountTypeAndCustomer(@RequestParam AccountType accountType,
                                                                                 @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {
        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));
        CompletableFuture<List<AccountDto>> accounts = accountService.getAccountsByCustomerAndAccountType(customerId, accountType);
        return ResponseEntity.ok(accounts.get());
    }

    //TODO Подумать что можно отправить в качестве ответа, мб просто в header запихивать инфу о причине ошибки, допустим,
    // если не хватает денег, то в header ответа записать "not_enough_money":"true". Что-то в этом роде
    // переводимые средства округляются до двух знаков после запятой, это в сервисе происходит
    @PutMapping("/transfers")
    public  ResponseEntity<Boolean> transferFromAccountToAccount(String serialFrom,
                                                                 String serialTo,
                                                                 double amount,
                                                                 @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {

        // Если клиент пытается перевести со счета на этот же счет
        if (serialFrom.equals(serialTo)){
            return (ResponseEntity.badRequest().header("same_serial", "true").build());
        }

        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));

        AccountTransferResponse response = accountService.transferFromAccountToAccount(serialFrom, serialTo, amount, customerId).get();

            if(response.isAccountNotExhist()){
                return ResponseEntity.notFound().build();
            }

            if(response.isNotCustomersAccount()){
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            if(response.isNotEnoughMoney()){
                return ResponseEntity.badRequest().header("not_enough_money","true").build();
            }

            if(response.isImpossibleTransfer()){
                return ResponseEntity.badRequest().header("impossible_transfer", "true").build();
            }

            if(response.isTransferError()){
                return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
            }

            return ResponseEntity.ok(null);
    }

    @PutMapping("/refils")
    //TODO Округлять amount пополнения
    public ResponseEntity<Boolean> refilAccountBalance(String serial,
                                                       double amount,
                                                       @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {

        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));
        AccountTransferResponse response = accountService.refilAccountBalance(serial, amount, customerId).get();

        if(response.isAccountNotExhist()) {
            return ResponseEntity.notFound().build();
        }

        if(response.isNotCustomersAccount()){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if(response.isTransferError()){
            return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
        }

        return ResponseEntity.ok(true);
    }

    @PutMapping("/withdraws")
    //TODO Округлять amount вывода
    public ResponseEntity<Boolean> withdrawAccountBalance(String serial,
                                                          double amount,
                                                          @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {

        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));

        AccountTransferResponse response = accountService.withdrawAccountBalance(serial,amount, customerId).get();

            if(response.isAccountNotExhist()){
                return ResponseEntity.notFound().build();
            }

            if(response.isNotCustomersAccount()){
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            if(response.isNotEnoughMoney()){
                return ResponseEntity.badRequest().header("not_enough_money","true").body(false);
            }

            if(response.isTransferError()){
                return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
            }

            return ResponseEntity.ok(true);
    }

    @PostMapping("/")
    public ResponseEntity<Boolean> createAccount(@RequestBody  AccountCreationRequestModel creationRequest,
                                                                    @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {
        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));

        creationRequest.setCustomerId(customerId);

        Boolean result = accountService.createAccount(creationRequest).get();

            if (!result){
                return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
            }

            return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{serial}")
    public  ResponseEntity<Boolean> deleteAccount(@PathVariable String serial, @NonNull HttpServletRequest request) throws ExecutionException, InterruptedException {

        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractId(accessCookie.getValue()));

        AccountDeleteResponse response = accountService.deleteAccount(serial, customerId).get();


            // Такого аккаунта нет, тут, по идее ничего не надо в header пихать
            if(response.isAccountNotFound()){
                return ResponseEntity.notFound().build();
            }

            // у данного клиента нет доступа к счету с таким номером
            if(response.isNotCustomerAccount()){
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            // отправляет badRequest, если на балансе есть деньги, а в header записывает причину такого ответа, то-есть "balance_not_empty":"true"
            if(response.isBalanceNotEmpty()){
                return ResponseEntity.badRequest().header("balance_not_empty","true").build();
            }

            // ошибка при сохранении информации в бд
            if(response.isDeletionError()){
                return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
            }

            return ResponseEntity.ok(true);
    }
}
