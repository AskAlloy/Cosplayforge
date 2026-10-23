package com.horizon.Cosplayforge.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String logradouro,
        String bairro,
        String localidade, // é o nome da cidade no ViaCEP
        String uf,
        Boolean erro
) {
}