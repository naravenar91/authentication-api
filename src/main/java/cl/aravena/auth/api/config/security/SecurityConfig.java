package cl.aravena.auth.api.config.security;

import cl.aravena.auth.api.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─────────────────────────────────────────────
    //  CORS — permite peticiones desde Angular :4200
    // ─────────────────────────────────────────────
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos (agregar producción cuando corresponda)
        config.setAllowedOrigins(List.of(
                "http://localhost:4200"
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));

        // Headers que puede enviar el frontend
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        // Permite enviar cookies / credentials si se necesitan
        config.setAllowCredentials(true);

        // Tiempo que el browser cachea el resultado del preflight (segundos)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder reactiveJwtDecoder) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .pathMatchers("/api/auth/login", "/api/auth/create").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder))
                        //Token inválido o malformado
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders()
                                    .setContentType(MediaType.APPLICATION_JSON);
                            ApiError error = new ApiError(
                                    "AUTH_001",
                                    "Invalid or expired token"
                            );
                            try {
                                byte[] bytes = objectMapper.writeValueAsBytes(error);
                                return exchange.getResponse()
                                        .writeWith(Mono.just(
                                                exchange.getResponse()
                                                        .bufferFactory()
                                                        .wrap(bytes)
                                        ));
                            } catch (Exception e) {
                                return Mono.error(e);
                            }
                        })
                )
                //Sin token (no envió Authorization header)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, e) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders()
                                    .setContentType(MediaType.APPLICATION_JSON);
                            ApiError error = new ApiError(
                                    "AUTH_001",
                                    "Access denied: Authentication required"
                            );
                            try {
                                byte[] bytes = objectMapper.writeValueAsBytes(error);
                                return exchange.getResponse()
                                        .writeWith(Mono.just(
                                                exchange.getResponse()
                                                        .bufferFactory()
                                                        .wrap(bytes)
                                        ));
                            } catch (Exception ex2) {
                                return Mono.error(ex2);
                            }
                        })
                        //Token válido pero sin permisos suficientes (ADMIN)
                        .accessDeniedHandler((exchange, e) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            exchange.getResponse().getHeaders()
                                    .setContentType(MediaType.APPLICATION_JSON);
                            ApiError error = new ApiError(
                                    "AUTH_403",
                                    "You do not have permission to perform this action."
                            );
                            try {
                                byte[] bytes = objectMapper.writeValueAsBytes(error);
                                return exchange.getResponse()
                                        .writeWith(Mono.just(
                                                exchange.getResponse()
                                                        .bufferFactory()
                                                        .wrap(bytes)
                                        ));
                            } catch (Exception ex2) {
                                return Mono.error(ex2);
                            }
                        })
                )
                .build();
    }
}
