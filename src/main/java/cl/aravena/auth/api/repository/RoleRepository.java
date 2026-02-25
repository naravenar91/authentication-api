package cl.aravena.auth.api.repository;

import cl.aravena.auth.api.entity.RoleEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<RoleEntity, Integer> {

    // Consulta para obtener los nombres de los roles (ej: ADMIN, USER) por el UUID del usuario
    @Query("SELECT r.name FROM roles r " +
            "JOIN authentication_roles ar ON r.id = ar.role_id " +
            "WHERE ar.auth_uuid = :authUuid")
    Flux<String> findRoleNamesByAuthUuid(String authUuid);

    Mono<RoleEntity> findByName(String name);
}