package com.quocchung.cntt1.techcycle_system.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * save refresh token to Redis
   *
   * @param userId
   * @param deviceId
   * @param tokenId
   * @param ttlMs
   */
  public void saveRefreshToken(Long userId, String deviceId, String tokenId, long ttlMs) {
    redisTemplate.opsForValue()
        .set(refreshTokenKey(userId, deviceId), tokenId, Duration.ofMillis(ttlMs));
  }

  /**
   * Check refresh token have valid
   *
   * @param userId
   * @param deviceId
   * @param tokenId
   * @return
   */
  public boolean isRefreshTokenValid(Long userId, String deviceId, String tokenId) {
    Object value = redisTemplate.opsForValue().get(refreshTokenKey(userId, deviceId));
    return value != null && tokenId.equals(String.valueOf(value));
  }

  /**
   * Remove refresh token from Redis
   *
   * @param userId
   * @param deviceId
   */
  public void deleteRefreshToken(Long userId, String deviceId) {
    redisTemplate.delete(refreshTokenKey(userId, deviceId));
  }

  /**
   * access token to blacklist with time to live (ms)
   *
   * @param accessToken
   * @param ttlMs
   */
  public void blacklistAccessToken(String accessToken, long ttlMs) {
    if (ttlMs <= 0) {
      return;
    }
    redisTemplate.opsForValue().set(blacklistKey(accessToken), "1", Duration.ofMillis(ttlMs));
  }

  /**
   * Check access token haved revoke (or exists redis blacklist)
   *
   * @param accessToken
   * @return
   */
  public boolean isAccessTokenBlacklisted(String accessToken) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(accessToken)));
  }

  /**
   * set value with time to live
   *
   * @param key
   * @param value
   * @param ttlSeconds
   */
  public void setValue(String key, String value, long ttlSeconds) {
    redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
  }

  /**
   * get value from key
   *
   * @param key
   * @return
   */
  public String getValue(String key) {
    Object value = redisTemplate.opsForValue().get(key);
    return value == null ? null : String.valueOf(value);
  }

  /**
   * delete key from redis
   *
   * @param key
   */
  public void delete(String key) {
    redisTemplate.delete(key);
  }

  /**
   * Generate a valid key for the refresh token.
   *
   * @param userId
   * @param deviceId
   * @return
   */
  private String refreshTokenKey(Long userId, String deviceId) {
    return "refresh_token:" + userId + ":" + deviceId;
  }

  /**
   * Create a valid key for the access token in preparation for adding it to the blacklist.
   *
   * @param accessToken
   * @return
   */
  private String blacklistKey(String accessToken) {
    return "blacklist:" + sha256(accessToken);
  }

  /**
   * Hàm hash một key để đưa vào redis
   *
   * @param value
   * @return
   */
  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is not available", ex);
    }
  }

  /**
   * count request in time = windownSecound
   * @param key
   * @param windowSeconds
   * @return
   */
  public long incrementCounter(String key, long windowSeconds) {
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1L) {
      redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
    }
    return count == null ? 0L : count;
  }

  /**
   * get timeTolive in key
   * @param key
   * @return
   */
  public long getTtlSeconds(String key) {
    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
    return ttl == null || ttl < 0 ? 0L : ttl;
  }
}
