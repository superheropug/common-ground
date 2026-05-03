package com.example.backend.Services;

import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;


public class JWTService {
    private static SecretKey secretKey;

    public static void setSecretKey(SecretKey sc){
        secretKey = sc;
    }

    public static String getUsernameFromJWT(String jwt) throws JwtException{
        if (jwt == null || jwt.isEmpty()) throw new JwtException("No auth token passed");
        Jws<Claims> claims;
        String sub;
            claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt); // Parsing auto-fails on exception.
            sub = claims.getPayload().getSubject();
        return sub;
    }
    public static String createJWTFromUsername(String username){
        // 3600000 is 1 hr
        return Jwts.builder().subject(username).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 3600000)).signWith(secretKey, Jwts.SIG.HS256).compact();
    }
}
