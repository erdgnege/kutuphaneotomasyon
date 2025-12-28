package com.kutuphane.otomasyon.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Kanka bu secret key senin imzan. Gerçek projede bunu application.properties
    // içine sakla.
    // En az 256-bit bir anahtar olmalı.
    private static final String SECRET_KEY = "68746172206B616E6B6120627520636F6B206775636C7520626972206B6579206F6C6D616C69";

    // Token içinden kullanıcı adını (subject) çekiyoruz
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Kullanıcı bilgileriyle token oluşturuyoruz (10 saatlik ömür biçtim)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 Saat
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Admin için 5 dakikalık token oluştur
    public String generateAdminToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, 1000L * 60 * 5); // 5 dakika
    }

    // Özel expiration süresi ile token oluştur
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationMs) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Token hala geçerli mi? (Hem kullanıcı adı doğru mu hem süresi bitmiş mi?)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}