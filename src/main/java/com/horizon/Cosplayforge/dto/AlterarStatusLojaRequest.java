package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.StatusLoja;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusLojaRequest(

    @NotNull(message = "Status é obrigatório")
    StatusLoja status

) {
}
