package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ChangePasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ForgotPasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.LoginRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.RegisterRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Auth.ResetPasswordRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.AuthSessionResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.LogoutResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Auth.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
  RegisterResponse register(RegisterRequest request);

  AuthSessionResponse login(LoginRequest request, String deviceId, boolean checkLogin);

  AuthSessionResponse refreshToken(String refreshToken, String deviceId);

  LogoutResponse logout(String accessToken, String refreshToken, String deviceId);

  void forgotPassword(ForgotPasswordRequest request, String clientIp);

  void resetPassword(ResetPasswordRequest request);

  void changePassword(HttpServletRequest httpRequest, ChangePasswordRequest request);

  String generateAuthUrl(String loginType, String backendBaseUrl);

  AuthSessionResponse loginWithGoogle(String code, String deviceId, String backendBaseUrl);

  AuthSessionResponse loginWithFacebook(String code, String deviceId, String backendBaseUrl);
}
