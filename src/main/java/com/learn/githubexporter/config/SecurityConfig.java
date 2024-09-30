package com.learn.githubexporter.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the GitHub Exporter application.
 * <p>
 * This class configures the security settings, including HTTP basic
 * authentication,
 * user details management, and password encoding using BCrypt.
 * </p>
 */
@Configuration
public class SecurityConfig {

  /**
   * Configures the security filter chain for HTTP requests.
   *
   * @param http the HttpSecurity object to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs while configuring security
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authz -> authz
            .anyRequest().authenticated() // All requests require authentication
        )
        .httpBasic(withDefaults()) // Enable HTTP Basic Authentication with default settings
        .csrf(csrf -> csrf.disable()); // Disable CSRF protection for simplicity

    return http.build();
  }

  /**
   * Provides an in-memory user details service with a hardcoded user.
   *
   * @return UserDetailsService configured with an admin user
   */
  @Bean
  public UserDetailsService userDetailsService() {
    // Use BCrypt to encode the password
    PasswordEncoder passwordEncoder = passwordEncoder();
    UserDetails user = User.builder()
        .username("admin")
        .password(passwordEncoder.encode("password")) // Encode the password
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(user);
  }

  /**
   * Configures a password encoder using BCrypt for secure password hashing.
   *
   * @return PasswordEncoder instance for password encoding
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // Create a PasswordEncoder bean
  }
}
