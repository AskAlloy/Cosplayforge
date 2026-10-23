package com.horizon.Cosplayforge.dto;

// resposta do login

public record LoginResponse(
		String token,
        String tipo,
        long expiraEmMinutos,
        UsuarioResponse usuario
		) {

}
