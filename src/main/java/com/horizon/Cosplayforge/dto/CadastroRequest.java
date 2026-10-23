package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.PerfilUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
		
		@NotBlank(message ="Nome é Obrigatorio")
		@Size (max = 100, message = "Nome deve ter no maximo 100 caracteres")
		String nome,
		
		// nome de usuario
		
		@NotBlank(message = "Nome de usuário é obrigatório")
        @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$",
                message = "Nome de usuário deve ter de 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado")
        String nomeUsuario,
        
        //email
        
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 180, message = "E-mail deve ter no máximo 180 caracteres")
        String email,
        
        //cpf
        
        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "^\\d{11}$", message = "CPF deve ter 11 dígitos, somente números")
        String cpf,
        
        // telefone
        
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,
        
        //Senha
        // Li que o Bcrypt tem q ser 72 no max.
        @NotBlank(message = "Senha é obrigatória")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "Senha deve ter de 8 a 72 caracteres, com ao menos uma letra e um número")
        String senha,
 
        @NotBlank(message = "Confirmação de senha é obrigatória")
        String confirmarSenha,
 
        @NotNull(message = "Perfil é obrigatório (CLIENTE ou VENDEDOR)")
        PerfilUsuario perfil
        
		
		) {

}
