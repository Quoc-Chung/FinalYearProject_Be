package com.quocchung.cntt1.techcycle_system.service.impl;

import static com.quocchung.cntt1.techcycle_system.constants.AppConstants.DEFAULT_ROLE_NAME;
import static com.quocchung.cntt1.techcycle_system.constants.Oauth2Contants.FACEBOOK;
import static com.quocchung.cntt1.techcycle_system.constants.Oauth2Contants.GOOGLE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ChangePasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ForgotPasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.LoginRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.RegisterRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ResetPasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.AuthSessionResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.AuthTokenResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.LogoutResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.RegisterResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Role;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserRole;
import com.quocchung.cntt1.techcycle_system.repository.RoleRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRoleRepository;
import com.quocchung.cntt1.techcycle_system.security.CustomUserDetailsService;
import com.quocchung.cntt1.techcycle_system.security.JwtService;
import com.quocchung.cntt1.techcycle_system.security.RedisTokenService;
import com.quocchung.cntt1.techcycle_system.service.AuthService;
import com.quocchung.cntt1.techcycle_system.service.EmailService;
import com.quocchung.cntt1.techcycle_system.utils.AppNormalize;
import com.quocchung.cntt1.techcycle_system.utils.Converter;
import com.quocchung.cntt1.techcycle_system.utils.enums.UserStatus;
import com.quocchung.cntt1.techcycle_system.utils.properties.ForgotPasswordProperites;
import com.quocchung.cntt1.techcycle_system.utils.properties.Oauth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final JwtService jwtService;
  private final RedisTokenService redisTokenService;
  private final AuthenticationManager authenticationManager;
  private final CustomUserDetailsService customUserDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final Converter converter;
  private final AppNormalize appNormalize;
  private final EmailService emailService;
  private final Oauth2Properties oauth2Properties;
  private final ForgotPasswordProperites forgotPasswordProperites;
  private final RestClient restClient = RestClient.create();
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * register user if user not account
   *
   * @param request email - fullName - password - confirmPassword
   * @return userId - email - fullName
   */
  @Override
  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResException(ResErrorCode.EMAIL_ALREADY_EXISTS);
    }
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      throw new ResException(
          ResErrorCode.PASSWORD_MISMATCH,
          "confirmPassword",
          "Password confirmation does not match"
      );
    }
    User user = User.builder()
        .email(request.getEmail().trim().toLowerCase())
        .password(passwordEncoder.encode(request.getPassword()))
        .fullName(request.getFullName())
        .status(UserStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .isFirstLogin(true)
        .build();
    user = userRepository.save(user);
    Role role = roleRepository.findByName(DEFAULT_ROLE_NAME)
        .orElseThrow(
            () -> new ResException(ResErrorCode.DEFAULT_ROLE_MISSING));
    if (!userRoleRepository.existsByUserUserIdAndRoleRoleId(user.getUserId(), role.getRoleId())) {
      userRoleRepository.save(UserRole.builder().user(user).role(role).build());
    }
    return RegisterResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .isFirstRegister(true)
        .build();
  }

  /**
   * login user when user register account
   *
   * @param request  email - password
   * @param deviceId deviceId
   * @return
   */
  @Override
  @Transactional
  public AuthSessionResponse login(LoginRequest request, String deviceId) {
    String email = request.getEmail().trim().toLowerCase();

    if (!userRepository.existsByEmail(email)) {
      throw new ResException(ResErrorCode.EMAIL_NOT_EXISTS, "email", "Tài khoản không tồn tại");
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, request.getPassword())
      );
    } catch (AuthenticationException ex) {
      throw new ResException(ResErrorCode.PASSWORD_INCORRECT, "password", "Mật khẩu không đúng");
    }
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResException(ResErrorCode.UNAUTHORIZED));

    switch (user.getStatus()) {
      case INACTIVE -> throw new ResException(ResErrorCode.USER_NOT_ACTIVE, "Tài khoản chưa được kích hoạt");
      case BANNED -> throw new ResException(ResErrorCode.USER_BANNED, "Tài khoản đã bị khóa");
      case DELETED -> throw new ResException(ResErrorCode.USER_DELETED, "Tài khoản đã bị xóa");
      case ACTIVE -> {}
    }
    AuthSessionResponse session = issueSession(user, appNormalize.normalizeDeviceId(deviceId));
    return session;
  }

  /**
   * refresh token when access token expire
   *
   * @param refreshToken refresh token when config yml
   * @param deviceId     device Id
   * @return
   */
  @Override
  @Transactional
  public AuthSessionResponse refreshToken(String refreshToken, String deviceId) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new ResException(ResErrorCode.UNAUTHORIZED);
    }
    try {
      if (!jwtService.isRefreshToken(refreshToken)) {
        throw new ResException(ResErrorCode.UNAUTHORIZED);
      }
      Long userId = jwtService.getUserId(refreshToken);
      String tokenId = jwtService.getTokenId(refreshToken);
      String tokenDeviceId = appNormalize.normalizeDeviceId(jwtService.getDeviceId(refreshToken),
          deviceId);

      if (!redisTokenService.isRefreshTokenValid(userId, tokenDeviceId, tokenId)) {
        throw new ResException(ResErrorCode.UNAUTHORIZED);
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResException(ResErrorCode.UNAUTHORIZED));
      if (user.getStatus() != UserStatus.ACTIVE) {
        throw new ResException(ResErrorCode.USER_NOT_ACTIVE);
      }
      return issueSession(user, tokenDeviceId);
    } catch (ResException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResException(ResErrorCode.UNAUTHORIZED);
    }
  }

  /**
   * logout account delete refresh token from redis if exists refresh token in redis blacklist
   * access token
   *
   * @param accessToken
   * @param refreshToken
   * @param deviceId
   * @return
   */
  @Override
  @Transactional
  public LogoutResponse logout(String accessToken, String refreshToken, String deviceId) {
    Long userId = null;

    if (refreshToken != null && !refreshToken.isBlank()) {
      try {
        if (jwtService.isRefreshToken(refreshToken)) {
          userId = jwtService.getUserId(refreshToken);
          String tokenDeviceId = appNormalize.normalizeDeviceId(
              jwtService.getDeviceId(refreshToken), deviceId);
          redisTokenService.deleteRefreshToken(userId, tokenDeviceId);
        }
      } catch (Exception ignored) {
        throw new ResException(ResErrorCode.GENERAL_ERROR);
      }
    }

    if (accessToken != null && !accessToken.isBlank()) {
      String normalizedToken =
          accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
      try {
        if (jwtService.isAccessToken(normalizedToken)) {
          if (userId == null) {
            userId = jwtService.getUserId(normalizedToken);
          }
          redisTokenService.blacklistAccessToken(normalizedToken,
              jwtService.getRemainingMillis(normalizedToken));
        }
      } catch (Exception ignored) {
        throw new ResException(ResErrorCode.GENERAL_ERROR);
      }
    }

    if (userId != null) {
      userRepository.findById(userId).ifPresent(user -> {
        user.setIsFirstLogin(false);
        userRepository.save(user);
      });
    }
    return LogoutResponse.builder()
        .message("Logged out successfully")
        .build();
  }

  @Override
  @Transactional
  public void forgotPassword(ForgotPasswordRequest request, String clientIp) {
    String normalizedEmail = normalizeEmail(request.getEmail());
    enforceForgotPasswordRateLimit(normalizedEmail, clientIp);

    Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
    if (userOptional.isEmpty()) {
      return;
    }
    User user = userOptional.get();
    if (user.getStatus() != UserStatus.ACTIVE) {
      return;
    }
    String rawToken = generateSecureToken();
    String tokenHash = sha256(rawToken);
    String tokenKey = "password_reset:token:" + tokenHash;
    String userKey = "password_reset:user:" + user.getUserId();
    String oldTokenHash = redisTokenService.getValue(userKey);
    if (oldTokenHash != null && !oldTokenHash.isBlank()) {
      redisTokenService.delete("password_reset:token:" + oldTokenHash);
    }

    redisTokenService.setValue(tokenKey, String.valueOf(user.getUserId()),
        forgotPasswordProperites.getResetTokenTtlSeconds());
    redisTokenService.setValue(userKey, tokenHash,
        forgotPasswordProperites.getResetTokenTtlSeconds());
    String resetPasswordLink =
        buildResetPasswordLink(rawToken);

    emailService.sendTemplateEmail(
        user.getEmail(),
        "Techcycle - Dat lai mat khau",
        "email/reset-password-email",
        Map.of(
            "name", user.getFullName(),
            "resetPasswordLink", resetPasswordLink,
            "expiredMinutes", Math.max(1L, forgotPasswordProperites.getResetTokenTtlSeconds() / 60)
        )
    );
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new ResException(
          ResErrorCode.PASSWORD_MISMATCH,
          "confirmPassword",
          "Password confirmation does not match"
      );
    }
    String tokenHash = sha256(request.getToken().trim());
    String tokenKey = "password_reset:token:" + tokenHash;
    String userIdValue = redisTokenService.getValue(tokenKey);
    if (userIdValue == null || userIdValue.isBlank()) {
      throw new ResException(ResErrorCode.RESET_TOKEN_INVALID);
    }
    Long userId = Long.valueOf(userIdValue);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.UNAUTHORIZED));
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    redisTokenService.delete(tokenKey);
    redisTokenService.delete("password_reset:user:" + userId);
  }

  @Override
  @Transactional
  public void changePassword(HttpServletRequest httpRequest, ChangePasswordRequest request) {
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new ResException(
          ResErrorCode.PASSWORD_MISMATCH,
          "confirmPassword",
          "Password confirmation does not match"
      );
    }

    String accessToken = extractBearerToken(httpRequest);
    if (redisTokenService.isAccessTokenBlacklisted(accessToken) || !jwtService.isAccessToken(
        accessToken)) {
      throw new ResException(ResErrorCode.UNAUTHORIZED);
    }

    Long userId = jwtService.getUserId(accessToken);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.UNAUTHORIZED));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new ResException(ResErrorCode.CURRENT_PASSWORD_INCORRECT, "currentPassword",
          "Current password is incorrect");
    }
    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
      throw new ResException(ResErrorCode.NEW_PASSWORD_MUST_DIFFER, "newPassword",
          "New password must be different");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    redisTokenService.blacklistAccessToken(accessToken, jwtService.getRemainingMillis(accessToken));
  }

  @Override
  public String generateAuthUrl(String loginType, String backendBaseUrl) {
    String normalizedType = loginType == null ? "" : loginType.trim().toLowerCase();
    String state = UUID.randomUUID().toString();

    return switch (normalizedType) {
      case GOOGLE -> buildAuthorizationUrl(
          oauth2Properties.getGoogleAuthorizationUri(),
          oauth2Properties.getGoogleClientId(),
          resolveRedirectUri(oauth2Properties.getGoogleRedirectUri(), backendBaseUrl),
          oauth2Properties.getGoogleScopes(),
          state
      );
      case FACEBOOK -> buildAuthorizationUrl(
          oauth2Properties.getFacebookAuthorizationUri(),
          oauth2Properties.getFacebookClientId(),
          resolveRedirectUri(oauth2Properties.getFacebookRedirectUri(), backendBaseUrl),
          oauth2Properties.getFacebookScopes(),
          state
      );
      default -> throw new ResException(
          ResErrorCode.UNSUPPORTED_SOCIAL_TYPE,
          "login_type",
          "Unsupported social login type: " + normalizedType
      );
    };
  }

  @Override
  @Transactional
  public AuthSessionResponse loginWithGoogle(String code, String deviceId, String backendBaseUrl) {
    String accessToken = exchangeCodeForAccessTokenGoogle(
        oauth2Properties.getGoogleTokenUri(),
        oauth2Properties.getGoogleClientId(),
        oauth2Properties.getGoogleClientSecret(),
        resolveRedirectUri(oauth2Properties.getGoogleRedirectUri(), backendBaseUrl),
        code,
        true
    );
    Map<String, Object> userInfo = fetchGoogleUserInfo(accessToken);
    return socialLogin(
        safeValue(userInfo.get("email")),
        safeValue(userInfo.get("name")),
        safeValue(userInfo.get("picture")),
        deviceId
    );
  }

  @Override
  @Transactional
  public AuthSessionResponse loginWithFacebook(String code, String deviceId,
      String backendBaseUrl) {
    String accessToken = exchangeCodeForAccessTokenFacebook(
        oauth2Properties.getFacebookTokenUri(),
        oauth2Properties.getFacebookClientId(),
        oauth2Properties.getFacebookClientSecret(),
        resolveRedirectUri(oauth2Properties.getFacebookRedirectUri(), backendBaseUrl),
        code
    );
    log.info("tes chung: " + resolveRedirectUri(oauth2Properties.getFacebookRedirectUri(),
        backendBaseUrl));
    Map<String, Object> userInfo = fetchFacebookUserInfo(accessToken);

    String facebookId = safeValue(userInfo.get("id"));
    if (facebookId == null || facebookId.isBlank()) {
      throw new ResException(ResErrorCode.SOCIAL_ACCOUNT_NO_EMAIL);
    }

    String email = "facebook_" + facebookId + "@facebook.com";
    String fullName = safeValue(userInfo.get("name"));

    String avatarUrl = fetchFacebookAvatarUrlSafe(facebookId);

    return socialLogin(email, fullName, avatarUrl, deviceId);
  }

  private String fetchFacebookAvatarUrlSafe(String facebookId) {
    try {
      String url = "https://graph.facebook.com/" + facebookId +
                   "/picture?width=500&height=500&redirect=false";

      String responseBody = restClient.get()
          .uri(url)
          .retrieve()
          .body(String.class);

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> response = mapper.readValue(responseBody, Map.class);

      Map<String, Object> data = (Map<String, Object>) response.get("data");

      Boolean isSilhouette = (Boolean) data.get("is_silhouette");

      if (Boolean.TRUE.equals(isSilhouette)) {
        return null;
      }

      return (String) data.get("url");

    } catch (Exception e) {
      log.warn("Facebook avatar error: {}", e.getMessage());
      return null;
    }
  }

  /**
   * @param user
   * @param deviceId
   * @return
   */
  private AuthSessionResponse issueSession(User user, String deviceId) {
    boolean isFirstLogin = Boolean.TRUE.equals(user.getIsFirstLogin());
    Set<GrantedAuthority> grantedAuthorities = customUserDetailsService.buildAuthorities(
        user.getUserId());
    Set<String> authorities = grantedAuthorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(java.util.stream.Collectors.toSet());
    String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getEmail(),
        authorities);

    String refreshId = jwtService.newRefreshTokenId();
    String refreshToken = jwtService.generateRefreshToken(user.getUserId(), user.getEmail(),
        deviceId, refreshId);

    redisTokenService.saveRefreshToken(user.getUserId(), deviceId, refreshId,
        jwtService.getRefreshTokenExpirationMs());
    if (Boolean.TRUE.equals(user.getIsFirstLogin())) {
      user.setIsFirstLogin(false);
    }
    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);

    AuthTokenResponse tokenResponse = AuthTokenResponse.builder()
        .accessToken(accessToken)
        .tokenType("Bearer")
        .isFirstLogin(isFirstLogin)
        .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
        .user(converter.mapToUserResponse(user))
        .build();

    return AuthSessionResponse.builder()
        .token(tokenResponse)
        .refreshToken(refreshToken)
        .build();
  }

  private void enforceForgotPasswordRateLimit(String email, String clientIp) {
    String normalizedIp = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp.trim();
    String emailRateKey = "rate_limit:forgot_password:email:" + sha256(email);
    String ipRateKey = "rate_limit:forgot_password:ip:" + sha256(normalizedIp);

    long emailCount = redisTokenService.incrementCounter(emailRateKey,
        forgotPasswordProperites.getForgotPasswordRateLimitWindowSeconds());
    if (emailCount > forgotPasswordProperites.getForgotPasswordEmailMaxAttempts()) {
      long waitSeconds = redisTokenService.getTtlSeconds(emailRateKey);
      throw new ResException(
          ResErrorCode.FORGOT_PASSWORD_RATE_LIMIT,
          new Object[]{Math.max(1L, waitSeconds)}
      );
    }

    long ipCount = redisTokenService.incrementCounter(ipRateKey,
        forgotPasswordProperites.getForgotPasswordRateLimitWindowSeconds());
    if (ipCount > forgotPasswordProperites.getForgotPasswordIpMaxAttempts()) {
      long waitSeconds = redisTokenService.getTtlSeconds(ipRateKey);
      throw new ResException(
          ResErrorCode.FORGOT_PASSWORD_RATE_LIMIT,
          new Object[]{Math.max(1L, waitSeconds)}
      );
    }
  }

  private String buildResetPasswordLink(String rawToken) {
    String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    String separator =
        forgotPasswordProperites.getFrontendResetPasswordUrl().contains("?") ? "&" : "?";
    return forgotPasswordProperites.getFrontendResetPasswordUrl() + separator + "token="
           + encodedToken;
  }

  private String normalizeEmail(String email) {
    if (email == null) {
      return "";
    }
    return email.trim().toLowerCase();
  }

  private String buildAuthorizationUrl(
      String authorizationUri,
      String clientId,
      String redirectUri,
      List<String> scopes,
      String state
  ) {
    String scope = scopes == null ? "" : String.join(" ", scopes);
    return authorizationUri
           + "?client_id=" + urlEncode(clientId)
           + "&redirect_uri=" + urlEncode(redirectUri)
           + "&response_type=code"
           + "&scope=" + urlEncode(scope)
           + "&state=" + urlEncode(state);
  }

  private String exchangeCodeForAccessTokenGoogle(
      String tokenUri,
      String clientId,
      String clientSecret,
      String redirectUri,
      String code,
      boolean includeGrantType
  ) {
    if (code == null || code.isBlank()) {
      throw new ResException(ResErrorCode.AUTH_CODE_REQUIRED, "code",
          "Authorization code is required");
    }
    MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
    payload.add("client_id", clientId);
    payload.add("client_secret", clientSecret);
    payload.add("redirect_uri", redirectUri);
    payload.add("code", code.trim());
    if (includeGrantType) {
      payload.add("grant_type", "authorization_code");
    }
    try {
      Map<String, Object> response = restClient.post()
          .uri(tokenUri)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(payload)
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {
          });
      String accessToken = safeValue(response == null ? null : response.get("access_token"));
      if (accessToken == null || accessToken.isBlank()) {
        throw new ResException(ResErrorCode.SOCIAL_TOKEN_EXCHANGE_FAILED);
      }
      return accessToken;
    } catch (ResException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResException(ResErrorCode.SOCIAL_TOKEN_EXCHANGE_FAILED);
    }
  }

  private String exchangeCodeForAccessTokenFacebook(
      String tokenUri,
      String clientId,
      String clientSecret,
      String redirectUri,
      String code
  ) {

    if (code == null || code.isBlank()) {
      throw new ResException(
          ResErrorCode.AUTH_CODE_REQUIRED,
          "code",
          "Authorization code is required"
      );
    }

    try {
      Map<String, Object> response = restClient.get()
          .uri(tokenUri +
               "?client_id=" + clientId +
               "&client_secret=" + clientSecret +
               "&redirect_uri=" + redirectUri +
               "&code=" + code.trim()
          )
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {
          });

      if (response == null) {
        throw new ResException(ResErrorCode.EMPTY_RESPONSE);
      }

      String accessToken = safeValue(response.get("access_token"));

      if (accessToken == null || accessToken.isBlank()) {
        throw new ResException(ResErrorCode.SOCIAL_TOKEN_EXCHANGE_FAILED);
      }

      return accessToken;

    } catch (HttpClientErrorException ex) {
      String errorBody = ex.getResponseBodyAsString();
      throw new ResException(ResErrorCode.SOCIAL_TOKEN_EXCHANGE_FAILED);
    } catch (ResException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResException(ResErrorCode.SOCIAL_TOKEN_EXCHANGE_FAILED);
    }
  }


  private Map<String, Object> fetchGoogleUserInfo(String accessToken) {
    try {
      return restClient.get()
          .uri(oauth2Properties.getGoogleUserInfoUri())
          .headers(headers -> headers.setBearerAuth(accessToken))
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {
          });
    } catch (Exception ex) {
      throw new ResException(ResErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
    }
  }

  private Map<String, Object> fetchFacebookUserInfo(String accessToken) {
    String uri = oauth2Properties.getFacebookUserInfoUri();
    String separator = uri.contains("?") ? "&" : "?";
    try {
      String responseBody = restClient.get()
          .uri(uri + separator + "access_token=" + accessToken)
          .retrieve()
          .body(String.class);
      if (responseBody == null || responseBody.isBlank()) {
        throw new ResException(ResErrorCode.EMPTY_RESPONSE);
      }

      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      Map<String, Object> userInfo = objectMapper.readValue(responseBody,
          new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
          });

      if (userInfo.containsKey("error")) {
        Object error = userInfo.get("error");
        throw new ResException(ResErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
      }

      return userInfo;

    } catch (ResException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResException(ResErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
    }
  }

  private AuthSessionResponse socialLogin(
      String email,
      String fullName,
      String avatarUrl,
      String deviceId
  ) {
    String normalizedEmail = normalizeEmail(email);
    if (normalizedEmail.isBlank()) {
      throw new ResException(ResErrorCode.SOCIAL_ACCOUNT_NO_EMAIL);
    }

    User user = userRepository.findByEmail(normalizedEmail)
        .orElseGet(() -> createSocialUser(normalizedEmail, fullName, avatarUrl));

    if (avatarUrl != null && !avatarUrl.isBlank() && (user.getAvatarUrl() == null
                                                      || user.getAvatarUrl().isBlank())) {
      user.setAvatarUrl(avatarUrl);
      user = userRepository.save(user);
    }

    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new ResException(ResErrorCode.USER_NOT_ACTIVE);
    }
    return issueSession(user, appNormalize.normalizeDeviceId(deviceId));
  }

  /**
   * Create social user
   *
   * @param email
   * @param fullName
   * @param avatarUrl
   * @return
   */
  private User createSocialUser(String email, String fullName, String avatarUrl) {
    User newUser = User.builder()
        .email(email)
        .password(passwordEncoder.encode(generateSecureToken()))
        .fullName(resolveDisplayName(fullName, email))
        .avatarUrl(avatarUrl)
        .status(UserStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .isFirstLogin(true)
        .build();
    newUser = userRepository.save(newUser);
    assignDefaultRole(newUser);
    return newUser;
  }

  /**
   * Assign default role "user" when user login with google or facebook
   *
   * @param user
   */
  private void assignDefaultRole(User user) {
    Role role = roleRepository.findByName(DEFAULT_ROLE_NAME)
        .orElseThrow(() -> new ResException(ResErrorCode.DEFAULT_ROLE_MISSING));
    if (!userRoleRepository.existsByUserUserIdAndRoleRoleId(user.getUserId(), role.getRoleId())) {
      userRoleRepository.save(UserRole.builder().user(user).role(role).build());
    }
  }

  /**
   * resolve displayname
   *
   * @param fullName
   * @param email
   * @return
   */
  private String resolveDisplayName(String fullName, String email) {
    if (fullName != null && !fullName.isBlank()) {
      return fullName.trim();
    }
    int atPos = email.indexOf("@");
    return atPos > 0 ? email.substring(0, atPos) : email;
  }

  private String resolveRedirectUri(String configuredRedirectUri, String backendBaseUrl) {
    if (configuredRedirectUri == null) {
      return "";
    }
    if (configuredRedirectUri.contains("{baseUrl}")) {
      return configuredRedirectUri.replace("{baseUrl}", backendBaseUrl);
    }
    return configuredRedirectUri;
  }

  private String safeValue(Object value) {
    if (value == null) {
      return null;
    }
    String normalized = value.toString().trim();
    return normalized.isBlank() ? null : normalized;
  }

  private String urlEncode(String value) {
    return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
  }

  private String extractBearerToken(HttpServletRequest httpRequest) {
    String authorization = httpRequest.getHeader("Authorization");
    if (authorization == null || authorization.isBlank()) {
      throw new ResException(ResErrorCode.UNAUTHORIZED);
    }
    return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
  }

  /**
   * generate token
   *
   * @return
   */
  private String generateSecureToken() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /**
   * encypt string param -> string hash (not decode)
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
}
