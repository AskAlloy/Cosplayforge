package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.PerfilUsuario;

import jakarta.validation.constraints.NotNull;


public record AlterarPerfilRequest(
		@NotNull(message = "Perfil é obrigatório (Cliente, Vendedor ou Administrador)")
		PerfilUsuario perfil
		) {

}
