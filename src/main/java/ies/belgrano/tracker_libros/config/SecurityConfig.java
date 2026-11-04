
package ies.belgrano.tracker_libros.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(
                                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                .dispatcherTypeMatchers(
                                                                DispatcherType.ERROR)
                                                .permitAll()

                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/registro",
                                                                "/api/auth/login")
                                                .permitAll()

                                                .requestMatchers(
                                                                 HttpMethod.GET,
                                                                 "/api/libros/buscar-isbn/*")
                                                .permitAll()

                                                .requestMatchers(
                                                                 HttpMethod.GET,
                                                                 "/api/gutenberg/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/libros/*")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/libros/*")
                                                .hasRole("ADMIN")

                                                .anyRequest().authenticated())

                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                                                .jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter())))

                                .httpBasic(basic -> basic.disable())
                                .formLogin(form -> form.disable());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                JwtGrantedAuthoritiesConverter authorities =
                                new JwtGrantedAuthoritiesConverter();
                authorities.setAuthoritiesClaimName("rol");
                authorities.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter converter =
                                new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(authorities);
                return converter;
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration configuration) throws Exception {

                return configuration.getAuthenticationManager();
        }
}
