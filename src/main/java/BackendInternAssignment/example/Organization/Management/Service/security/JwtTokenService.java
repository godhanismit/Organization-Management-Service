package BackendInternAssignment.example.Organization.Management.Service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenService {

    private final SecretKey secretKey;
    private final Duration tokenTtl;

    public JwtTokenService(@Value("${app.jwt.secret}") String secret,
                           @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenTtl = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(tokenTtl);
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Instant extractExpiry(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().toInstant();
    }
}

