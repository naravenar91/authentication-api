package cl.aravena.auth.api.config.security;

import cl.aravena.auth.api.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder reactiveJwtDecoder) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
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
