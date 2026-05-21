package com.quocchung.cntt1.techcycle_system.utils.properties;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@Validated
public class Oauth2Properties {

  // Google
  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  @NotBlank
  private String googleClientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  @NotBlank
  private String googleClientSecret;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String googleRedirectUri;

  @Value("${spring.security.oauth2.client.registration.google.scope}")
  private List<String> googleScopes;

  @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
  @NotBlank
  private String googleAuthorizationUri;

  @Value("${spring.security.oauth2.client.provider.google.token-uri}")
  @NotBlank
  private String googleTokenUri;

  @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
  @NotBlank
  private String googleUserInfoUri;


  // Facebook
  @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
  @NotBlank
  private String facebookClientId;

  @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
  @NotBlank
  private String facebookClientSecret;

  @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
  private String facebookRedirectUri;

  @Value("#{'${spring.security.oauth2.client.registration.facebook.scope}'.split(',')}")
  private List<String> facebookScopes;

  @Value("${spring.security.oauth2.client.provider.facebook.authorization-uri}")
  @NotBlank
  private String facebookAuthorizationUri;

  @Value("${spring.security.oauth2.client.provider.facebook.token-uri}")
  @NotBlank
  private String facebookTokenUri;

  @Value("${spring.security.oauth2.client.provider.facebook.user-info-uri}")
  @NotBlank
  private String facebookUserInfoUri;
}
