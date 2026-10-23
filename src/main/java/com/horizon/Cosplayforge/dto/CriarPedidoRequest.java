package com.horizon.Cosplayforge.dto;

import jakarta.validation.constraints.NotNull;

public record CriarPedidoRequest(
        @NotNull(message = "Endereço de entrega é obrigatório")
        Long enderecoId
) {
}
