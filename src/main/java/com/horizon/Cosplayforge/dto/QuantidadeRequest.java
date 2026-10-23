package com.horizon.Cosplayforge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// para definir o estoque com esse valor
public record QuantidadeRequest(
        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 0, message = "Quantidade deve ser de pelo menos 1")
        Integer quantidade
) {
}
