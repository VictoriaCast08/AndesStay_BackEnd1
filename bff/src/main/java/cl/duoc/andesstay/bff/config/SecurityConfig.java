package cl.duoc.andesstay.bff.config;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtRoleConverter jwtRoleConverter;

    public SecurityConfig(JwtRoleConverter jwtRoleConverter) {
        this.jwtRoleConverter = jwtRoleConverter;
    }

    @Bean
    @Profile("local")
    SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Profile("azure")
    SecurityFilterChain azureSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bff/ping").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtRoleConverter)))
                .build();
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) ->
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized",
                        "Autenticación rechazada",
                        messageOrDefault(authException != null ? authException.getMessage() : null,
                                "No se proporcionó un token válido"));
    }

    private AccessDeniedHandler forbiddenHandler() {
        return (request, response, accessDeniedException) ->
                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden",
                        "Acceso denegado",
                        messageOrDefault(accessDeniedException != null ? accessDeniedException.getMessage() : null,
                                "El rol del token no tiene permiso para este endpoint"));
    }

    private static String messageOrDefault(String message, String fallback) {
        return (message == null || message.isBlank()) ? fallback : message;
    }

    private static void writeJson(HttpServletResponse response, int status, String error,
                                  String message, String reason) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String escapedReason = reason.replace("\\", "\\\\").replace("\"", "\\\"");
        String escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().print(
                "{\"status\":" + status
                        + ",\"error\":\"" + error + "\""
                        + ",\"message\":\"" + escapedMessage + "\""
                        + ",\"reason\":\"" + escapedReason + "\"}");
        response.getWriter().flush();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${andesstay.cors.allowed-origins:http://localhost:4200,http://localhost:5173}") String origins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(origins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("WWW-Authenticate"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Profile("azure")
    JwtDecoder azureJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${andesstay.azure.audience}") String audience) {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        String normalizedAudience = audience.startsWith("api://")
                ? audience.substring("api://".length())
                : audience;
        String apiAudience = "api://" + normalizedAudience;
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            if (!jwt.getAudience().contains(normalizedAudience) && !jwt.getAudience().contains(apiAudience)) {
                logger.warn("JWT audience rechazado. Recibido: {}, esperado: {}", jwt.getAudience(), audience);
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                        "invalid_token", "The token audience is not authorized", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        return jwtDecoder;
    }
}
