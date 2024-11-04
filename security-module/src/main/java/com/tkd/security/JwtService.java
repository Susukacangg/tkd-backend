package com.tkd.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final String jwtSecretKey;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        final long TOKEN_EXPIRATION_PERIOD = 900000;
        return buildToken(extraClaims, userDetails, TOKEN_EXPIRATION_PERIOD);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        final long REFRESH_TOKEN_EXPIRATION_PERIOD = 604800000;
        return buildToken(new HashMap<>(), userDetails, REFRESH_TOKEN_EXPIRATION_PERIOD);
    }

    // parseSignedClaims in extract username already checks if token is expired
    public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        final String currUsername = extractUsername(jwtToken);
        // compares the username in the JWT to the passed in username
        return (currUsername.equals(userDetails.getUsername()));
    }

    public String extractUsername(String jwtToken) throws IllegalArgumentException, AccountExpiredException {
        try {
            return extractClaim(jwtToken, Claims::getSubject);
        } catch (MalformedJwtException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(String.format("Invalid JWT token: %s", jwtToken));
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
            throw new AccountExpiredException(String.format("Expired JWT token: %s", jwtToken));
        }
    }

    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimsResolver.apply(claims);
    }


    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationPeriod) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationPeriod))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Claims extractAllClaims(String jwtToken) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
