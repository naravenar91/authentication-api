package cl.aravena.auth.api.config;

import cl.aravena.auth.api.entity.AuthenticationEntity;
import cl.aravena.auth.api.repository.AuthenticationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AdminInitializer {
    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final AuthenticationRepository authenticationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.user-id}")
    private String adminUserId;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.uuid}")
    private String adminUuid;

    public AdminInitializer(AuthenticationRepository authenticationRepository,
                            PasswordEncoder passwordEncoder) {
        this.authenticationRepository = authenticationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createAdminIfNotExists() {
        authenticationRepository.findByUserId(adminUserId)
                .switchIfEmpty(
                        // No existe → lo creamos con la misma lógica que tu save()
                        createAdmin().flatMap(authenticationRepository::save)
                )
                .subscribe(
                        entity -> log.info("Default administrator user successfully created: {}", entity.getUserId()),
                        error  -> log.error("Error initializing administrator user: {}", error.getMessage())
                );
    }

    private Mono<AuthenticationEntity> createAdmin() {
        AuthenticationEntity entity = new AuthenticationEntity();
        entity.setUserId(adminUserId);
        entity.setPassword(passwordEncoder.encode(adminPassword));
        entity.setUuid(adminUuid);
        entity.setIsActive(true);
        entity.setRole("ADMIN");
        entity.setNew(true);

        log.info("Initial Admin user created...");
        return reactor.core.publisher.Mono.just(entity);
    }
}
