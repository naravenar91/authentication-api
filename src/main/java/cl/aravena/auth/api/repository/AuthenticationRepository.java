package cl.aravena.auth.api.repository;

import cl.aravena.auth.api.entity.AuthenticationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface AuthenticationRepository extends ReactiveCrudRepository<AuthenticationEntity, String> {

    @Query("SELECT * FROM authentication WHERE user_id = :userName AND password = :password AND is_active = true")
    Mono<AuthenticationEntity> login(@Param("userName") String userName, @Param("password") String password);

    Mono<AuthenticationEntity> findByUserId(String userId);

    //Mono<AuthenticationRequest> findByUserIdAndPasswordAndIsActiveTrue(String userId, String password);
}