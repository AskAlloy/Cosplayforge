package com.horizon.Cosplayforge.model;

public enum StatusLoja {
	EM_ANALISE,
    ATIVA,
    SUSPENSA,
    CANCELADA;

    public boolean podeIrPara(StatusLoja destino) {
        return switch (this) {
            case EM_ANALISE -> destino == ATIVA || destino == CANCELADA;
            case ATIVA -> destino == SUSPENSA || destino == CANCELADA;
            case SUSPENSA -> destino == ATIVA || destino == CANCELADA;
            case CANCELADA -> false;
        };
    }
}
