package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.StatusPedido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarStatusPedidoRequest(
        @NotNull(message = "Status é obrigatório")
        StatusPedido status,

        // Obrigatório só ao marcar ENVIADO (regra no Service).
        @Size(max = 50, message = "Código de rastreio deve ter no máximo 50 caracteres")
        String codigoRastreio
) {
}
