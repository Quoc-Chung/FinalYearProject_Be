package com.quocchung.cntt1.techcycle_system.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.quocchung.cntt1.techcycle_system.security.CustomUserDetailsService;
import com.quocchung.cntt1.techcycle_system.security.JwtAuthenticationFilter;
import com.quocchung.cntt1.techcycle_system.security.RestAccessDeniedHandler;
import com.quocchung.cntt1.techcycle_system.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableConfigurationProperties({AuthProperties.class, MinioProperties.class})
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomUserDetailsService customUserDetailsService;
  private final RestAuthenticationEntryPoint authenticationEntryPoint;
  private final RestAccessDeniedHandler accessDeniedHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(withDefaults())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()

            .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

            .requestMatchers("/ws/**").permitAll()

            .requestMatchers(HttpMethod.GET, "/api/post/page").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post/all-post-data").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post/search").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post/get-latest-posts").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post/search-category").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post/post-detail-user/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post/hot-post").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/post/user/**").permitAll()
            // Category - public
            .requestMatchers(HttpMethod.GET, "/api/category/**").permitAll()
            // Brand - public
            .requestMatchers(HttpMethod.GET, "/api/brand/**").permitAll()
            // TabHome - public (following requires auth, but others don't)
            .requestMatchers(HttpMethod.GET, "/api/tabhome/popular-searches").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tabhome/today-activity").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tabhome/newest-posts").permitAll()
            // User public profile
            .requestMatchers(HttpMethod.GET, "/api/user/profile/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/user/seller-info/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/user/trust-score/**").permitAll()
            // Reactions - public (view reactions without login)
            .requestMatchers(HttpMethod.GET, "/api/reactions/post/**").permitAll()
            // All other requests require authentication
            .anyRequest().authenticated()
        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(customUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }
}
