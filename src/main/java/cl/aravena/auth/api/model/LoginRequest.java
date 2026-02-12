package cl.aravena.auth.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "111111111", description = "User Name")
        @NotBlank
        String userName,

        @Schema(example = "juanito", description = "Password")
        @NotBlank
        String password
) {}