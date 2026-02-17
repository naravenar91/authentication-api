package cl.aravena.auth.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateUserRequest(
        //String uuid,
        @Schema(example = "111111111", description = "User Name")
        String userName,
        @Schema(example = "juanito", description = "Password")
        String password,
        @Schema(example = "true", description = "Is Active")
        Boolean isActive,
        @Schema(example = "USER", description = "Role user", allowableValues = {"USER", "ADMIN", "OPER"})
        String role
) {}