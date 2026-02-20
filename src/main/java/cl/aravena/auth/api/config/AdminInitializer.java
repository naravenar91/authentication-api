package cl.aravena.auth.api.config;

import cl.aravena.auth.api.entity.AuthenticationEntity;
import cl.aravena.auth.api.repository.AuthenticationRepository;
import cl.aravena.auth.api.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AdminInitializer {
    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final AuthenticationRepository authenticationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DatabaseClient databaseClient;

    @Value("${app.admin.user-id}")
    private String adminUserId;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.uuid}")
    private String adminUuid;

    public AdminInitializer(AuthenticationRepository authenticationRepository, RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder, DatabaseClient databaseClient) {
        this.authenticationRepository = authenticationRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.databaseClient = databaseClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        authenticationRepository.findByUserId(adminUserId)
                .switchIfEmpty(Mono.defer(() ->
                        createAdmin()
                                .flatMap(admin -> authenticationRepository.save(admin)
                                        .flatMap(savedAdmin ->
                                                // Buscamos el ID del rol ADMIN en la tabla 'roles'
                                                roleRepository.findByName("ADMIN")
                                                        .flatMap(role ->
                                                                // Insertamos la relación en la tabla intermedia
                                                                databaseClient.sql("INSERT INTO authentication_roles (auth_uuid, role_id) VALUES (:uuid, :roleId)")
                                                                        .bind("uuid", savedAdmin.getUuid())
                                                                        .bind("roleId", role.getId())
                                                                        .fetch()
                                                                        .rowsUpdated()
                                                        )
                                                        .thenReturn(savedAdmin)
                                        )
                                )
                ))
                .subscribe(
                        entity -> log.info("Default administrator user successfully created/verified: {}", entity.getUserId()),
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
