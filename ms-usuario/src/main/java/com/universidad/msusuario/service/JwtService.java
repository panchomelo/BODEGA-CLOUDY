package com.universidad.msusuario.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

// ═══════════════════════════════════════════════════
// Firma (HS256) y validación de JWT con secreto compartido.
// Solo ms-usuario firma; los demás MS validan con su copia
// del mismo secreto (ver JwtValidator en cada uno).
// ═══════════════════════════════════════════════════
@Slf4j
@Service
public class JwtService {

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration:3600}") long accessExpiration,
            @Value("${jwt.refresh-expiration:604800}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccess(String username) {
        return build(username, accessExpiration);
    }

    public String generateRefresh(String username) {
        return build(username, refreshExpiration);
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    private String build(String username, long seconds) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                // jti aleatorio: dos tokens del mismo user en el mismo segundo
                // serían idénticos sin esto (jjwt usa precisión de segundos)
                // y chocarían con la columna única de refresh_tokens.
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + seconds * 1000))
                .signWith(key)
                .compact();
    }

    // Lanza JwtException si firma/expiración inválidas.
    public String validateAndGetUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
