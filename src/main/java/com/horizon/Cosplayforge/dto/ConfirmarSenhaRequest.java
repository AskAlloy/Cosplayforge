package com.horizon.Cosplayforge.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarSenhaRequest(
		@NotBlank(message = "Informe a senha para confirmar")
        String senha
		) {

}
