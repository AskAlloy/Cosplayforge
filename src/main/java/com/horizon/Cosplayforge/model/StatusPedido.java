package com.horizon.Cosplayforge.model;

import java.util.List;

public enum StatusPedido {
	
	PENDENTE,
    PAGAMENTO_EM_ANALISE,
    PAGAMENTO_RECUSADO,
    PAGO,
    EM_PRODUCAO,
    PRONTO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    public static final List<StatusPedido> FINAIS = List.of(ENTREGUE, CANCELADO);
	
	public boolean podeIrPara(StatusPedido destino) {
    return switch (this) {
        case PENDENTE -> destino == PAGAMENTO_EM_ANALISE || destino == CANCELADO;
        case PAGAMENTO_EM_ANALISE -> destino == PAGO || destino == PAGAMENTO_RECUSADO;
        case PAGAMENTO_RECUSADO -> destino == PENDENTE || destino == CANCELADO;
        case PAGO -> destino == EM_PRODUCAO || destino == PRONTO;
        case EM_PRODUCAO -> destino == PRONTO;
        case PRONTO -> destino == ENVIADO;
        case ENVIADO -> destino == ENTREGUE;
        case ENTREGUE, CANCELADO -> false;
    };
    }

}
