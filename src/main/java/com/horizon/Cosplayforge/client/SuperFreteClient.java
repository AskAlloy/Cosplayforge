package com.horizon.Cosplayforge.client;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.horizon.Cosplayforge.exception.ServicoExternoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SuperFreteClient {

    private final RestClient restClient;

    public SuperFreteClient(
            @Value("${superfrete.url}") String url,
            @Value("${superfrete.token}") String token,
            @Value("${superfrete.user-agent}") String userAgent) {

        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(3));
        timeouts.setReadTimeout(Duration.ofSeconds(8));

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(timeouts)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<SuperFreteOpcao> cotar(SuperFreteCotacaoRequest request) {
        SuperFreteOpcao[] resposta;
        try {
            resposta = restClient.post()
                    .uri("/api/v0/calculator")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(SuperFreteOpcao[].class);
        } catch (RestClientException e) {
            log.warn("Falha ao cotar frete na SuperFrete: {}", e.toString());
            throw new ServicoExternoException(
                    "Cálculo de frete indisponível no momento. Tente novamente em instantes.");
        }

        if (resposta == null) {
            return List.of();
        }
        return Arrays.stream(resposta).filter(SuperFreteOpcao::valida).toList();
    }
}