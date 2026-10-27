package com.curso.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final SecretKey key;
    private final String issuer;
    private final long expirationSeconds;

    public JwtTokenService(@Value("${app.jwt.secret}") String secret,
                           @Value("${app.jwt.issuer}") String issuer,
                           @Value("${app.jwt.expiration}") long expirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(AppUserDetails details) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expirationSeconds * 1000L);
        List<String> permisos = List.copyOf(details.getPermisos());

        return Jwts.builder()
                .issuer(issuer)
                .subject(details.getUsername())
                .claim("rol", details.getRol().name())
                .claim("permisos", permisos)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
