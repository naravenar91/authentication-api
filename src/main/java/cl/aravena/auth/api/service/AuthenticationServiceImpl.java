package cl.aravena.auth.api.service;

import cl.aravena.auth.api.entity.AuthenticationEntity;
import cl.aravena.auth.api.exception.UserAlreadyExistsException;
import cl.aravena.auth.api.model.AuthenticationResponse;
import cl.aravena.auth.api.model.CreateUserRequest;
import cl.aravena.auth.api.repository.AuthenticationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationRepository authenticationRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(AuthenticationRepository authenticationRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationRepository = authenticationRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<AuthenticationResponse> login(String userName, String password) {

        return authenticationRepository.findByUserId(userName) // 1. Buscamos solo por usuario
            .filter(entity -> passwordEncoder.matches(password, entity.getPassword())) // 2. Validamos el hash
            //.map(entity -> modelMapper.map(entity, AuthenticationResponse.class)) // 3. Mapeamos si es correcto
            .map(entity -> {
                // Generar JWT
                String token = jwtService.generateToken(entity.getUserId(), entity.getUuid(), entity.getRole());
                // Mapear y agregar token
                AuthenticationResponse response = modelMapper.map(entity, AuthenticationResponse.class);
                response.setToken(token); // <-- agregamos el token
                return response;
            })
            .switchIfEmpty(Mono.error(new BadCredentialsException("Incorrect username or password")));
    }

    @Override
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
                    entity.setRole(request.role());
                    //INDICAMOS QUE ES UN REGISTRO NUEVO
                    entity.setNew(true);

                    return authenticationRepository.save(entity);
                }))
                .map(savedEntity -> modelMapper.map(savedEntity, AuthenticationResponse.class));
    }

    @Override
    public Flux<AuthenticationResponse> getAll() {
        return authenticationRepository.findAll()
                .map(entity -> modelMapper.map(entity, AuthenticationResponse.class));
    }
}
