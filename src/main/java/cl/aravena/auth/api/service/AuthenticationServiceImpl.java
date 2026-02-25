package cl.aravena.auth.api.service;

import cl.aravena.auth.api.entity.AuthenticationEntity;
import cl.aravena.auth.api.entity.AuthenticationRolesEntity;
import cl.aravena.auth.api.exception.UserAlreadyExistsException;
import cl.aravena.auth.api.model.AuthenticationResponse;
import cl.aravena.auth.api.model.CreateUserRequest;
import cl.aravena.auth.api.repository.AuthenticationRepository;
import cl.aravena.auth.api.repository.AuthenticationRolesRepository;
import cl.aravena.auth.api.repository.RoleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationRepository authenticationRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationRolesRepository authRolesRepository;

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(AuthenticationRepository authenticationRepository, RoleRepository roleRepository, AuthenticationRolesRepository authRolesRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationRepository = authenticationRepository;
        this.roleRepository = roleRepository;
        this.authRolesRepository = authRolesRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<AuthenticationResponse> login(String userName, String password) {
        return authenticationRepository.findByUserId(userName)
                .filter(entity -> passwordEncoder.matches(password, entity.getPassword()))
                .flatMap(entity ->
                        // BUSCAMOS LOS ROLES ANTES DE GENERAR EL TOKEN
                        roleRepository.findRoleNamesByAuthUuid(entity.getUuid())
                                .collectList() // Esto crea la List<String> que JwtService espera
                                .map(roles -> {
                                    // Si la lista viene vacía, podrías asignar 'USER' por defecto aquí si quisieras
                                    if (roles.isEmpty()) roles = List.of("USER");

                                    // Generar JWT con la lista de roles real
                                    String token = jwtService.generateToken(entity.getUserId(), entity.getUuid(), roles);

                                    AuthenticationResponse response = modelMapper.map(entity, AuthenticationResponse.class);
                                    response.setToken(token);
                                    return response;
                                })
                )
                .switchIfEmpty(Mono.error(new BadCredentialsException("Incorrect username or password")));
    }

    @Override
    @Transactional
    public Mono<AuthenticationResponse> save(CreateUserRequest request) {
        return authenticationRepository.findByUserId(request.userName())
                .flatMap(existingUser -> Mono.<AuthenticationEntity>error(
                        new UserAlreadyExistsException("The user '" + request.userName() + "' is already registered")
                ))
                .switchIfEmpty(Mono.defer(() -> {
                    AuthenticationEntity entity = modelMapper.map(request, AuthenticationEntity.class);
                    entity.setUserId(request.userName());
                    entity.setPassword(passwordEncoder.encode(request.password()));
                    entity.setUuid(java.util.UUID.randomUUID().toString());
                    entity.setIsActive(true);
                    // Opcional: Guardar el primer rol o un string unido en la tabla principal si la columna persiste
                    entity.setRole(request.roles().get(0));
                    entity.setNew(true);

                    return authenticationRepository.save(entity)
                            .flatMap(savedUser ->
                                    // Convertimos la lista de nombres de roles en un Flux
                                    Flux.fromIterable(request.roles())
                                            .flatMap(roleName ->
                                                    roleRepository.findByName(roleName)
                                                            .switchIfEmpty(Mono.error(new RuntimeException("Role not found: " + roleName)))
                                                            .flatMap(roleEntity -> {
                                                                AuthenticationRolesEntity relation = AuthenticationRolesEntity.builder()
                                                                        .authUuid(savedUser.getUuid())
                                                                        .roleId(roleEntity.getId())
                                                                        .isNewEntry(true)
                                                                        .build();
                                                                return authRolesRepository.save(relation);
                                                            })
                                            )
                                            .then(Mono.just(savedUser)) // Al terminar todos los inserts, devolvemos el usuario
                            );
                }))
                .map(savedEntity -> modelMapper.map(savedEntity, AuthenticationResponse.class));
    }

    @Override
    public Flux<AuthenticationResponse> getAll() {
        return authenticationRepository.findAll()
                .map(entity -> modelMapper.map(entity, AuthenticationResponse.class));
    }
}
