package com.horizon.Cosplayforge.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		
		@NotBlank(message = "Informe o e-mail ou nome de usuario")
		String login,
		
		@NotBlank(message = "Informe a senha")
		String senha
		) {

}
