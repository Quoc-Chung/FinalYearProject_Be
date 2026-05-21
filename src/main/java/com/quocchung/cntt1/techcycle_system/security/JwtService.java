package com.quocchung.cntt1.techcycle_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey signingKey;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;

  public JwtService(
      @Value("${jwt.secret}") String jwtSecret,
      @Value("${jwt.access-token-expiration}") long accessTokenExpirationMs,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationMs
  ) {
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
  }

  public String generateAccessToken(Long userId, String email, Collection<String> authorities) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(email)
        .claim("uid", userId)
        .claim("type", "access")
        .claim("authorities", authorities)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
        .signWith(signingKey)
        .compact();
  }

  public String generateRefreshToken(Long userId, String email, String deviceId, String tokenId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(email)
        .claim("uid", userId)
        .claim("type", "refresh")
        .claim("deviceId", deviceId)
        .id(tokenId)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(refreshTokenExpirationMs)))
        .signWith(signingKey)
        .compact();
  }

  public String newRefreshTokenId() {
    return UUID.randomUUID().toString();
  }

  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean isAccessToken(String token) {
    return "access".equals(parseClaims(token).get("type", String.class));
  }

  public boolean isRefreshToken(String token) {
    return "refresh".equals(parseClaims(token).get("type", String.class));
  }

  public Long getUserId(String token) {
    return parseClaims(token).get("uid", Long.class);
  }

  public String getEmail(String token) {
    return parseClaims(token).getSubject();
  }

  public String getDeviceId(String token) {
    return parseClaims(token).get("deviceId", String.class);
  }

  public String getTokenId(String token) {
    return parseClaims(token).getId();
  }

  public List<String> getAuthorities(String token) {
    Claims claims = parseClaims(token);
    Object raw = claims.get("authorities");
    if (raw instanceof List<?> list) {
      return list.stream().map(String::valueOf).toList();
    }
    return List.of();
  }

  public long getAccessTokenExpirationMs() {
    return accessTokenExpirationMs;
  }

  public long getRefreshTokenExpirationMs() {
    return refreshTokenExpirationMs;
  }

  public long getRemainingMillis(String token) {
    long expirationMs = parseClaims(token).getExpiration().getTime();
    return Math.max(0L, expirationMs - System.currentTimeMillis());
  }
}
