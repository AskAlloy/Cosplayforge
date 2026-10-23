package com.horizon.Cosplayforge.dto;

import java.util.UUID;

import com.horizon.Cosplayforge.model.PerfilUsuario;
import com.horizon.Cosplayforge.model.Usuario;

public record UsuarioResponse(
		UUID id,
        String nome,
        String nomeUsuario,
        String email,
        String telefone,
        PerfilUsuario perfil,
        boolean emailConfirmado,
        boolean inativo
		) {
	public static UsuarioResponse de(Usuario user) {
		return new UsuarioResponse(
				user.getId(),
                user.getNome(),
                user.getNomeUsuario(),
                user.getEmail(),
                user.getTelefone(),
                user.getPerfil(),
                Boolean.TRUE.equals(user.getEmailConfirmado()),
                user.estaInativo()
				);
	}

}
