package com.horizon.Cosplayforge.service;

import org.springframework.stereotype.Component;

import com.horizon.Cosplayforge.client.ViaCepClient;
import com.horizon.Cosplayforge.client.ViaCepResponse;
import com.horizon.Cosplayforge.dto.EnderecoRequest;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.model.Endereco;
import com.horizon.Cosplayforge.util.Textos;

import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class MontadorEndereco {

    private final ViaCepClient viaCepClient;

    public void preencher(Endereco endereco, EnderecoRequest req) {
        String cep = req.cep().replace("-", "");
        ViaCepResponse via = viaCepClient.buscar(cep);

        String logradouro = preferirViaCep(via.logradouro(), req.logradouro(), "logradouro");
        String bairro = preferirViaCep(via.bairro(), req.bairro(), "bairro");

        endereco.setCep(cep);
        endereco.setLogradouro(logradouro);
        endereco.setBairro(bairro);
        endereco.setCidade(via.localidade());
        endereco.setUf(via.uf());
        endereco.setNumero(req.numero().trim());
        endereco.setComplemento(Textos.vazioParaNull(req.complemento()));
    }

    // CEPs gerais vêm do ViaCEP sem logradouro/bairro.
    private String preferirViaCep(String doViaCep, String doCliente, String campo) {
        String valor = Textos.vazioParaNull(doViaCep);
        if (valor == null) {
            valor = Textos.vazioParaNull(doCliente);
        }
        if (valor == null) {
            throw new RegraNegocioException(
                    "Este CEP não informa o " + campo + ". Envie o campo '" + campo + "' na requisição.");
        }
        return valor;
    }
}
