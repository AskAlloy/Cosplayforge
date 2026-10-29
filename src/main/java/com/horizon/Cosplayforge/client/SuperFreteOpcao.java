package com.horizon.Cosplayforge.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SuperFreteOpcao (
        Integer id,
        String name,
        BigDecimal price,
        @JsonProperty("delivery_time") Integer tempoEntrega,
        @JsonProperty("has_errors") Boolean temErro
) {

    public boolean valida(){
        return !Boolean.TRUE.equals(temErro) && price != null;
    }

}
