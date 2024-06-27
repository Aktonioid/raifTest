package com.raifTest.infrastructure.controllers;

import com.raifTest.core.dto.CustomerDto;
import com.raifTest.core.mappers.CustomerMapper;
import com.raifTest.core.models.Customer;
import com.raifTest.core.models.RefreshTokenModel;
import com.raifTest.core.responseModels.CustomerDeletionResponse;
import com.raifTest.core.services.ICustomerService;
import com.raifTest.core.services.IJwtService;
import com.raifTest.core.services.IRefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final ICustomerService customerService;
    private final IJwtService jwtService;
    private final IRefreshTokenService refreshTokenService;
    private final CustomerMapper mapper;

    long refreshTokenExpitaion = 1000 * 60 * 60 *24 * 7;

    @Autowired
    public CustomerController(ICustomerService customerService, IJwtService jwtService, IRefreshTokenService refreshTokenService){
        this.customerService = customerService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.mapper = new CustomerMapper();
    }

    //Выводит accessToken, чтоб можно было спокойно поместить его в авторизацию swagger
    @PutMapping()
    public ResponseEntity<String> signIn(String username, String password, HttpServletResponse response) throws ExecutionException, InterruptedException {

        if(!customerService.singIn(username, password).get()) return ResponseEntity.badRequest().build();


        CompletableFuture<ResponseEntity<String>> result = customerService.getCustomerByUsername(username)
                .thenApply(s ->{
                    UUID tokenId = UUID.randomUUID();

                    String accessToken = jwtService.generateAccessToken(mapper.asEntity(s), tokenId);
                    String refreshToken = jwtService.generateRefreshToken(mapper.asEntity(s), tokenId);

                    refreshTokenService.createToken(refreshToken, new Date(System.currentTimeMillis()+refreshTokenExpitaion),tokenId);

                    Cookie refreshCookie = new Cookie("refresh", refreshToken);
                    refreshCookie.setHttpOnly(true);

                    Cookie accessCookie = new Cookie("access", accessToken);
                    accessCookie.setHttpOnly(true);

                    response.addCookie(refreshCookie);
                    response.addCookie(accessCookie);

                    return  ResponseEntity.ok(accessToken);
                });

        return result.get();
    }

    @PostMapping()
    public ResponseEntity<String> createCustomer(@NotNull @RequestBody CustomerDto customerDto, HttpServletResponse response) throws ExecutionException, InterruptedException {

        customerDto.setId(null);

        CompletableFuture<ResponseEntity<String>> result = customerService.createCustomer(customerDto)
                .thenApply(customerCreationResponse -> {

                    if(customerCreationResponse.isUsernameExist()){
                        return ResponseEntity.badRequest().build();
                    }

                    if(customerCreationResponse.isCreationError()){
                        return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
                    }

                    UUID tokenId = UUID.randomUUID();

                    String accessToken = jwtService.generateAccessToken(mapper.asEntity(customerDto), tokenId);
                    String refreshToken = jwtService.generateRefreshToken(mapper.asEntity(customerDto), tokenId);

                    refreshTokenService.createToken(refreshToken, new Date(System.currentTimeMillis()+refreshTokenExpitaion),tokenId);

                    Cookie refreshCookie = new Cookie("refresh", refreshToken);
                    refreshCookie.setHttpOnly(true);

                    Cookie accessCookie = new Cookie("access", accessToken);
                    accessCookie.setHttpOnly(true);

                    response.addCookie(refreshCookie);
                    response.addCookie(accessCookie);

                    return ResponseEntity.ok(accessToken);
                });

        return result.get();
    }


    @PutMapping("/refreshes")
    public ResponseEntity<String> RefreshToken(@NonNull  HttpServletResponse response, @NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {

        return CompletableFuture.supplyAsync(() ->{

            Cookie[] cookies = request.getCookies();
            Cookie accessCookie = Arrays.stream(cookies).filter(c -> c.getName().equals("access")).findFirst().get();
            Cookie refreshCookie = Arrays.stream(cookies).filter(c -> c.getName().equals("refresh")).findFirst().get();

            if(accessCookie == null || refreshCookie == null){
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            String accessToken = accessCookie.getValue();
            String refreshToken = refreshCookie.getValue();

            if(!jwtService.isTokenValidNoTime(accessToken))
            {
                setCookieToEmpty(accessCookie, refreshCookie, response);
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            UUID tokenId = UUID.fromString(jwtService.extractTokenId(accessToken));

            RefreshTokenModel refreshTokenModel = null;
            try {
                refreshTokenModel = refreshTokenService.getTokenById(tokenId).get();
            } catch (InterruptedException e) {
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            } catch (ExecutionException e) {
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            if(refreshTokenModel == null){
                setCookieToEmpty(accessCookie, refreshCookie, response);
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            if(!refreshTokenModel.getToken().equals(refreshToken)){
                setCookieToEmpty(accessCookie, refreshCookie, response);
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            if(refreshTokenModel.getExpiredDate().before(new Date(System.currentTimeMillis()))){
                setCookieToEmpty(accessCookie, refreshCookie, response);
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            Customer customer = new Customer(UUID.fromString(jwtService.extractId(accessToken)));

            tokenId = UUID.randomUUID();

            accessToken = jwtService.generateAccessToken(customer, tokenId);
            refreshToken = jwtService.generateRefreshToken(customer, tokenId);

            refreshTokenService.createToken(refreshToken, new Date(System.currentTimeMillis()+refreshTokenExpitaion),tokenId);

            refreshCookie = new Cookie("refresh", refreshToken);
            refreshCookie.setHttpOnly(true);

            accessCookie = new Cookie("access", accessToken);
            accessCookie.setHttpOnly(true);

            response.addCookie(refreshCookie);
            response.addCookie(accessCookie);

            return ResponseEntity.ok(accessToken);
        }).get();

    }

    //Костыль, чтоб можно было в настройках ограничить доступ
    @DeleteMapping("/delete")
    public ResponseEntity<Boolean> deleteUser(@NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {
        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractTokenId(accessCookie.getValue()));

        CustomerDeletionResponse customerDeletionResponse = customerService.deleteCustomerById(customerId).get();
        if(customerDeletionResponse.isCustomerHasAccounts()){
            return ResponseEntity.badRequest().header("has_accounts","true").build();
        }

        if (customerDeletionResponse.isUserNotFound()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if(customerDeletionResponse.isDeletiionError()){
            return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
        }

        return ResponseEntity.ok(true);
    }

    @GetMapping()
    public ResponseEntity<CustomerDto> getCurrentCustomer(@NonNull  HttpServletRequest request) throws ExecutionException, InterruptedException {
        Cookie accessCookie = Arrays
                .stream(request.getCookies())
                .filter(c -> c.getName().equals("access"))
                .findFirst().get();


        UUID customerId = UUID.fromString(jwtService.extractTokenId(accessCookie.getValue()));

        CompletableFuture<CustomerDto> result = customerService.getCustomerById(customerId);

        result.join();

        if(result.get() == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.get());
    }

    private void setCookieToEmpty(Cookie accessCookie, Cookie refreshCookie,HttpServletResponse response){
        refreshCookie = new Cookie("refresh", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);

        accessCookie = new Cookie("access", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0);

        response.addCookie(refreshCookie);
        response.addCookie(accessCookie);
    }


}
