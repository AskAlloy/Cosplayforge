package com.horizon.Cosplayforge.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ErroResposta(int status, String mensagem, List<String> detalhes, 
		LocalDateTime timestamp) {
	
	public static ErroResposta de(HttpStatus status, String mensagem) {
        return new ErroResposta(status.value(), mensagem, List.of(), LocalDateTime.now());
    }
 
    public static ErroResposta de(HttpStatus status, String mensagem, List<String> detalhes) {
        return new ErroResposta(status.value(), mensagem, detalhes, LocalDateTime.now());
    }

}
