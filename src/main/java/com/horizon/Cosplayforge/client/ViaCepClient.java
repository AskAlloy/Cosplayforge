package com.horizon.Cosplayforge.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.exception.ServicoExternoException;

import lombok.extern.slf4j.Slf4j;


@Slf4j // Lombok: cria o objeto "log" para escrever no console
@Component
public class ViaCepClient {

    private final RestClient restClient;

    public ViaCepClient(@Value("${viacep.url:https://viacep.com.br/ws}") String url) {
        // Timeouts: sem eles, um ViaCEP lento deixaria a nossa API pendurada.
        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(3));
        timeouts.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(timeouts)
                .build();
    }


    public ViaCepResponse buscar(String cep) {
        ViaCepResponse resposta;
        try {
            // GET {url}/{cep}/json/  ->  JSON convertido em ViaCepResponse
            resposta = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException e) {

            log.warn("Falha ao consultar o ViaCEP para o CEP {}: {}", cep, e.toString());
            throw new ServicoExternoException(
                    "Consulta de CEP indisponível no momento. Tente novamente em instantes.");
        }

        if (resposta == null || Boolean.TRUE.equals(resposta.erro())) {
            throw new RegraNegocioException("CEP não encontrado.");
        }
        return resposta;
    }
}