package com.raifTest.core.services;

import com.raifTest.core.models.Customer;

import java.util.UUID;

public interface IJwtService {
    public String extractUserName(String token);
    public String extractEmail(String token);
    public String extractId(String token);
    public String extractTokenId(String token);
    public String generateAccessToken(Customer user, UUID tokenId);
    public String generateRefreshToken(Customer user, UUID tokenId);
    public boolean isTokenValid(String token, Customer user);
    public boolean isTokenValidNoTime(String token);
}
