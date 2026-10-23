package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.Endereco;
import jakarta.persistence.Column;

public record EnderecoResponse(
        Long id,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {
    public static EnderecoResponse de(Endereco e) {
        return new EnderecoResponse(
                e.getId(),
                e.getCep(),
                e.getLogradouro(),
                e.getNumero(),
                e.getComplemento(),
                e.getBairro(),
                e.getCidade(),
                e.getUf());
    }
}
