package com.horizon.Cosplayforge.exception;

// http 404
public class RecursoNaoEncontradoException extends RuntimeException{
	public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
