package cl.aravena.auth.api.controler;

import cl.aravena.auth.api.model.AuthenticationResponse;
import cl.aravena.auth.api.model.CreateUserRequest;
import cl.aravena.auth.api.model.LoginRequest;
import cl.aravena.auth.api.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request.userName(), request.password())
                .map(authRequest -> ResponseEntity.ok(authRequest));
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<AuthenticationResponse>> create(@RequestBody CreateUserRequest request) {
        return authenticationService.save(request)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<AuthenticationResponse> getAll() {
        return authenticationService.getAll();
    }
}
