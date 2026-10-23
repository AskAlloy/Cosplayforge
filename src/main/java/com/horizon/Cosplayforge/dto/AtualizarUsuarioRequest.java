package com.horizon.Cosplayforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
 

public record AtualizarUsuarioRequest(
		@NotBlank(message = "Nome é Obrigatório")
		@Size(max = 100, message = "Nome deve ter no maximo 100 caracteres")
		String nome,

		@NotBlank(message = "Nome de usuário não pode estar em branco")
		@Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$",
	    message = "Nome de usuário deve ter de 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado")
		String nomeUsuario,
		
		@Size(max = 20,message = "Telefone deve ter no máximo 20 caracteres")
		String telefone
		) {
}
