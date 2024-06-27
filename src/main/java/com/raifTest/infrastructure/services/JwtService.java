package com.raifTest.infrastructure.services;


import com.raifTest.core.models.Customer;
import com.raifTest.core.services.IJwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.Base64;

@Service
public class JwtService implements IJwtService
{
    @Autowired
    Environment env;

    public long refreshTokenExpitaion = 1000 * 60 * 60 *24 * 7;

    @Override
    public String extractUserName(String token)
    {
        return ExtractClaims(token, Claims::getSubject);
    }

    @Override
    public String extractEmail(String token)
    {
        return ExtractAllClaims(token, env.getProperty("jwt.secret")).get("email").toString();
    }

    @Override
    public String extractId(String token)
    {
        return ExtractAllClaims(token, env.getProperty("jwt.secret")).get("id").toString();
    }

    @Override
    public String extractTokenId(String token)
    {
        return ExtractAllClaims(token, env.getProperty("jwt.secret")).get("token_id").toString();
    }

    @Override
    public String generateAccessToken(Customer user, UUID tokenId)
    {
        Map<String, Object> claims = new HashMap<>(); // claims

        claims.put("id", user.getId());
        claims.put("token_id", tokenId);

        return GenerateToken(claims, 
                            user, 
                            new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24), // токен валиден сутки 
                            GetKey(env.getProperty("jwt.secret"))
                            );
    }

    @Override
    public String generateRefreshToken(Customer user, UUID tokenId)
    {
        Map<String, Object> claims = new HashMap<>(); // claims

        claims.put("username", user.getUsername());
        claims.put("token_id", tokenId);

        return GenerateToken(claims,
                            user, 
                            new Date(System.currentTimeMillis() + (1000 * 60 * 60 *24 * 7)), //токен будет валиден неделю
                            GetKey(env.getProperty("refresh.secret")));
    }

    @Override
    public boolean isTokenValid(String token, Customer user)
    {
        final String username = extractUserName(token);

        return (username.equals(user.getUsername())) && !IsTokenExpired(token); 
    }
    

    @Override
    // проверка подписи
    public boolean isTokenValidNoTime(String token)
    {

        try
        {
           ExtractAllClaims(token, env.getProperty("jwt.secret"));
        }
        catch(SignatureException e)
        {
            return false;
        }
        catch(ExpiredJwtException e)
        {

        }

        return true;
    }


    private String GenerateToken(Map<String, Object> extraClaims, Customer user,
                                Date expirationDate, SecretKey key)
    {
        return Jwts.builder().claims(extraClaims).subject(user.getUsername())
                    .issuedAt(new Date())
                    .expiration(expirationDate)
                    .signWith(key, Jwts.SIG.HS512)
                    .compact();
    }
    


    private <T> T ExtractClaims(String token, Function<Claims, T> claimsResolver)
    {
        final Claims claims = ExtractAllClaims(token, env.getProperty("jwt.secret"));
        return claimsResolver.apply(claims);
    }

    private Claims ExtractAllClaims(String string, String secret)
    {
        return Jwts.parser().verifyWith(GetKey(secret)).build().parseSignedClaims(string).getPayload();
    }

    
    private SecretKey GetKey(String secret)
    {
        return new SecretKeySpec(
            Base64.getDecoder().decode(secret), 
            "HmacSHA512");
    }

    private boolean IsTokenExpired(String token)
    {
        return ExtractExpiration(token).before(new Date());
    }

    private Date ExtractExpiration(String token)
    {
        return ExtractClaims(token, Claims::getExpiration);
    }

    
}
