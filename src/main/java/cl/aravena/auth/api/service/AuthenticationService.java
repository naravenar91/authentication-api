package cl.aravena.auth.api.service;

import cl.aravena.auth.api.model.AuthenticationResponse;
import cl.aravena.auth.api.model.CreateUserRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<AuthenticationResponse> login(String userName, String password);
    Mono<AuthenticationResponse> save(CreateUserRequest request);
    Flux<AuthenticationResponse> getAll();
}
