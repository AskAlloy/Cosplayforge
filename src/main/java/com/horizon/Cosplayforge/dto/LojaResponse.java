package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.Loja;
import com.horizon.Cosplayforge.model.StatusLoja;

import java.util.UUID;

public record LojaResponse(
        Long id,
        String nome,
        String descricao,
        String cnpj,
        String emailContato,
        String telefone,
        EnderecoResponse endereco,
        StatusLoja status,
        UUID proprietarioId,
        String proprietarioNome
) {
    public static LojaResponse de(Loja loja) {
        return new LojaResponse(
                loja.getId(),
                loja.getNome(),
                loja.getDescricao(),
                loja.getCnpj(),
                loja.getEmailContato(),
                loja.getTelefone(),
                EnderecoResponse.de(loja.getEndereco()),
                loja.getStatus(),
                loja.getProprietario().getId(),
                loja.getProprietario().getNome());
    }
}
