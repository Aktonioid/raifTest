package com.raifTest.core.services;

import com.raifTest.core.models.RefreshTokenModel;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IRefreshTokenService {
    public CompletableFuture<RefreshTokenModel> getTokenById(UUID tokenId);
    public CompletableFuture<Boolean> createToken(String token, Date expirationDate, UUID tokenId);
    public CompletableFuture<Boolean> deleteTokenById(UUID tokenId);
    //раз в день удаляет токены из бд
    public CompletableFuture<Boolean> delteTokenByDate();
}
