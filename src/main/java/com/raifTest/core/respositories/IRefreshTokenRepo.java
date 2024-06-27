package com.raifTest.core.respositories;

import com.raifTest.core.models.RefreshTokenModel;

import java.util.Date;
import java.util.UUID;

public interface IRefreshTokenRepo {
    public RefreshTokenModel getTokenById(UUID tokenId); // получить токен из бд
    public boolean createToken(RefreshTokenModel token); // создание нового токена
    public boolean deleteTokenById(UUID tokenId); // удаление токена из бд
    public boolean delteTokensByExpirationDate(Date today );
}
