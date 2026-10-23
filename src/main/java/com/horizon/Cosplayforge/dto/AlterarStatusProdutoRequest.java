package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.StatusProduto;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusProdutoRequest(
        @NotNull(message = "Status é obrigatório")
        StatusProduto status
) {
}
