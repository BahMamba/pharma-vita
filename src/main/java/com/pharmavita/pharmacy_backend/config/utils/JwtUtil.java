package com.pharmavita.pharmacy_backend.config.utils;

import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Value(value = "${jwt.secret}")
    private String secret;

    private final long expiration = 3600000L;

    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
            .claims(new HashMap<>())
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS512)
            .compact();
    }

    public String extractUsername(String token){
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpiredToken(token);
    }

    private boolean isExpiredToken(String token){
        return getClaims(token).getExpiration().before(new Date());
    }

}
