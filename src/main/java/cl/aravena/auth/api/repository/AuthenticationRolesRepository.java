package cl.aravena.auth.api.repository;

import cl.aravena.auth.api.entity.AuthenticationRolesEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AuthenticationRolesRepository extends ReactiveCrudRepository<AuthenticationRolesEntity, String> {
}