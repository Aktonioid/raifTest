package com.raifTest.infrastructure.services;

import com.raifTest.core.models.RefreshTokenModel;
import com.raifTest.core.respositories.IRefreshTokenRepo;
import com.raifTest.core.services.IRefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class RefreshTokenService implements IRefreshTokenService {

    private final IRefreshTokenRepo refreshTokenRepo;

    @Autowired
    public RefreshTokenService(IRefreshTokenRepo refreshTokenRepo){
        this.refreshTokenRepo = refreshTokenRepo;
    }

    @Override
    @Async
    public CompletableFuture<RefreshTokenModel> getTokenById(UUID tokenId) {
        return CompletableFuture.supplyAsync(() -> refreshTokenRepo.getTokenById(tokenId));
    }

    @Override
    @Async
    public CompletableFuture<Boolean> createToken(String token, Date expirationDate, UUID tokenId) {
        return CompletableFuture.supplyAsync(() ->{
            RefreshTokenModel tokenModel = new RefreshTokenModel(tokenId,expirationDate, token);
            return refreshTokenRepo.createToken(tokenModel);
        });
    }

    @Override
    @Async
    public CompletableFuture<Boolean> deleteTokenById(UUID tokenId) {
        return CompletableFuture.supplyAsync(() ->{
            return refreshTokenRepo.deleteTokenById(tokenId);
        });
    }

    @Override
    @Async
    @Scheduled(cron = "0 0 0 * * *")
    public CompletableFuture<Boolean> delteTokenByDate() {
        return CompletableFuture.supplyAsync(() -> {
            return refreshTokenRepo.delteTokensByExpirationDate(new Date());
        });
    }
}
