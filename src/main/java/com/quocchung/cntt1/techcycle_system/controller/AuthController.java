package com.quocchung.cntt1.techcycle_system.controller;

import static com.quocchung.cntt1.techcycle_system.constants.AppConstants.DEVICE_HEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quocchung.cntt1.techcycle_system.config.AuthProperties;
import com.quocchung.cntt1.techcycle_system.utils.properties.Oauth2Properties;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ChangePasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ForgotPasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.LoginRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.RegisterRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ResetPasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.AuthSessionResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.AuthTokenResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.LogoutResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.RegisterResponse;
import com.quocchung.cntt1.techcycle_system.security.JwtService;
import com.quocchung.cntt1.techcycle_system.service.AuthService;
import com.quocchung.cntt1.techcycle_system.utils.CookieUtils;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;
  private final AuthProperties authProperties;
  private final JwtService jwtService;
  private final ResponseUtils responseUtils;
  private final CookieUtils cookieUtils;
  private final ObjectMapper objectMapper;

  @PostMapping("/register")
  public ResponseEntity<APIResponse<RegisterResponse>> register(
      @Valid @RequestBody RegisterRequest registerRequest,
      @RequestHeader(value = DEVICE_HEADER, required = false) String deviceId) {
    RegisterResponse data = authService.register(registerRequest);

    AuthSessionResponse session = authService.login(
        LoginRequest.builder()
            .email(registerRequest.getEmail())
            .password(registerRequest.getPassword())
            .build(),
        deviceId, false);

    ResponseCookie refreshCookie = ResponseCookie.from(authProperties.getRefreshCookieName(),
            session.getRefreshToken())
        .httpOnly(true)
        .secure(authProperties.isRefreshCookieSecure())
        .sameSite(authProperties.getRefreshCookieSameSite())
        .path(authProperties.getRefreshCookiePath())

        .maxAge(Math.max(1L, jwtService.getRemainingMillis(session.getRefreshToken()) / 1000))
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(responseUtils.success(data));
  }

  @PostMapping("/login")
  public ResponseEntity<APIResponse<AuthTokenResponse>> login(
      @Valid @RequestBody LoginRequest loginRequest,
      @RequestHeader(value = DEVICE_HEADER, required = false) String deviceId
  ) {
    AuthSessionResponse session = authService.login(loginRequest, deviceId, true);
    ResponseCookie refreshCookie = ResponseCookie.from(authProperties.getRefreshCookieName(),
            session.getRefreshToken())
        .httpOnly(true)
        .secure(authProperties.isRefreshCookieSecure())
        .sameSite(authProperties.getRefreshCookieSameSite())
        .path(authProperties.getRefreshCookiePath())
        .maxAge(Math.max(1L, jwtService.getRemainingMillis(session.getRefreshToken()) / 1000))
        .build();
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(responseUtils.success(session.getToken()));
  }


  @PostMapping("/refresh-token")
  public ResponseEntity<APIResponse<AuthTokenResponse>> refreshToken(
      HttpServletRequest request,
      @RequestHeader(value = DEVICE_HEADER, required = false) String deviceId
  ) {
    String refreshToken = cookieUtils.getRefreshTokenFromCookie(request);
    AuthSessionResponse session = authService.refreshToken(refreshToken, deviceId);

    ResponseCookie refreshCookie = ResponseCookie.from(authProperties.getRefreshCookieName(),
            session.getRefreshToken())
        .httpOnly(true)
        .secure(authProperties.isRefreshCookieSecure())
        .sameSite(authProperties.getRefreshCookieSameSite())
        .path(authProperties.getRefreshCookiePath())
        .maxAge(Math.max(1L, jwtService.getRemainingMillis(session.getRefreshToken()) / 1000))
        .build();
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(responseUtils.success(session.getToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<APIResponse<LogoutResponse>> logout(
      HttpServletRequest request,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @RequestHeader(value = DEVICE_HEADER, required = false) String deviceId
  ) {
    String refreshToken = cookieUtils.getRefreshTokenFromCookie(request);
    LogoutResponse data = authService.logout(authorization, refreshToken, deviceId);
    ResponseCookie clearRefreshToken = ResponseCookie.from(authProperties.getRefreshCookieName(),
            "")
        .httpOnly(true)
        .secure(authProperties.isRefreshCookieSecure())
        .sameSite(authProperties.getRefreshCookieSameSite())
        .path(authProperties.getRefreshCookiePath())
        .maxAge(0)
        .build();
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, clearRefreshToken.toString())
        .body(responseUtils.success(data));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<APIResponse<String>> forgotPassword(
      HttpServletRequest httpRequest,
      @Valid @RequestBody ForgotPasswordRequest request
  ) {
    authService.forgotPassword(request, resolveClientIp(httpRequest));
    return ResponseEntity.ok(responseUtils.success(
        "If the email exists, a password reset link has been sent"));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<APIResponse<String>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request
  ) {
    authService.resetPassword(request);
    return ResponseEntity.ok(responseUtils.success("Password reset"));
  }

  @PostMapping("/change-password")
  public ResponseEntity<APIResponse<String>> changePassword(
      HttpServletRequest request,
      @Valid @RequestBody ChangePasswordRequest req
  ) {
    authService.changePassword(request, req);
    return ResponseEntity.ok(responseUtils.success("Password changed"));
  }


  private String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }
    return request.getRemoteAddr();
  }


  @GetMapping("/social-login")
  public ResponseEntity<APIResponse<String>> socialAuth(
      @RequestParam("login_type") String loginType,
      HttpServletRequest request) {

    loginType = loginType.trim().toLowerCase();
    String backendBaseUrl = resolveBackendBaseUrl(request);
    String url = authService.generateAuthUrl(loginType, backendBaseUrl);

    return ResponseEntity.ok(responseUtils.success(url));
  }


@GetMapping("/google/callback")
public ResponseEntity<Void> handleGoogleCallback(
    @RequestParam("code") String code,
    HttpServletRequest request,
    @RequestHeader(value = DEVICE_HEADER, required = false) String deviceId
) {
  try {
    String backendBaseUrl = resolveBackendBaseUrl(request);
    AuthSessionResponse session = authService.loginWithGoogle(code, deviceId, backendBaseUrl);

    ResponseCookie refreshCookie = ResponseCookie.from(authProperties.getRefreshCookieName(),
            session.getRefreshToken())
        .httpOnly(true)
        .secure(authProperties.isRefreshCookieSecure())
        .sameSite(authProperties.getRefreshCookieSameSite())
        .path(authProperties.getRefreshCookiePath())
        .maxAge(Math.max(1L, jwtService.getRemainingMillis(session.getRefreshToken()) / 1000))
        .build();

    String accessToken = session.getToken().getAccessToken();
    String userJson = URLEncoder.encode(
        objectMapper.writeValueAsString(session.getToken().getUser()),
        StandardCharsets.UTF_8
    );

    String redirectUrl = String.format(
        "https://techcycleit.duckdns.org/auth/callback?accessToken=%s&user=%s",
        accessToken, userJson
    );

    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .header(HttpHeaders.LOCATION, redirectUrl)
        .build();

  } catch (JsonProcessingException e) {
    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, "https://techcycleit.duckdns.org/sign-in?error=social_login_failed")
        .build();
  }
}

  @GetMapping("/facebook/callback")
  public ResponseEntity<Void> handleFacebookCallback(
      @RequestParam("code") String code,
      HttpServletRequest request,
      @RequestHeader(value = DEVICE_HEADER, required = false) String deviceId
  ) {
    try {
      String backendBaseUrl = resolveBackendBaseUrl(request);
      AuthSessionResponse session = authService.loginWithFacebook(code, deviceId, backendBaseUrl);

      ResponseCookie refreshCookie = ResponseCookie.from(
              authProperties.getRefreshCookieName(),
              session.getRefreshToken())
          .httpOnly(true)
          .secure(authProperties.isRefreshCookieSecure())
          .sameSite(authProperties.getRefreshCookieSameSite())
          .path(authProperties.getRefreshCookiePath())
          .maxAge(Math.max(1L, jwtService.getRemainingMillis(session.getRefreshToken()) / 1000))
          .build();

      String accessToken = session.getToken().getAccessToken();
      String userJson = URLEncoder.encode(
          objectMapper.writeValueAsString(session.getToken().getUser()),
          StandardCharsets.UTF_8
      );

      String redirectUrl = String.format(
          "https://techcycleit.duckdns.org/auth/callback?accessToken=%s&user=%s",
          accessToken, userJson
      );

      return ResponseEntity.status(HttpStatus.FOUND)
          .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
          .header(HttpHeaders.LOCATION, redirectUrl)
          .build();

    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.FOUND)
          .header(HttpHeaders.LOCATION, "https://techcycleit.duckdns.org/sign-in?error=social_login_failed")
          .build();
    }
  }

  /**
   * Resolve backend base URL from request, supporting reverse proxy scenarios.
   * Uses X-Forwarded-* headers when present.
   */
  private String resolveBackendBaseUrl(HttpServletRequest request) {
    String contextPath = request.getContextPath() == null ? "" : request.getContextPath();

    String forwardedProto = request.getHeader("X-Forwarded-Proto");
    String forwardedHost = request.getHeader("X-Forwarded-Host");
    String forwardedPort = request.getHeader("X-Forwarded-Port");

    if (forwardedProto != null && forwardedHost != null) {
      StringBuilder url = new StringBuilder();
      url.append(forwardedProto).append("://").append(forwardedHost);
      if (forwardedPort != null && !forwardedPort.isBlank()) {
        int port = Integer.parseInt(forwardedPort.trim());
        boolean addPort = (forwardedProto.equalsIgnoreCase("http") && port != 80)
            || (forwardedProto.equalsIgnoreCase("https") && port != 443);
        if (addPort) {
          url.append(":").append(port);
        }
      }
      url.append(contextPath).append("/api");
      return url.toString();
    }

    // Fallback to request URI
    return ServletUriComponentsBuilder.fromRequestUri(request)
        .replacePath(contextPath + "/api")
        .replaceQuery(null)
        .build()
        .toUriString();
  }

}
