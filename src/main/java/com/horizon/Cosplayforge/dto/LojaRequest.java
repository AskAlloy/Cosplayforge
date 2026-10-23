package com.horizon.Cosplayforge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LojaRequest(
        @NotBlank(message = "Nome da loja é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
        String descricao,

        // CNPJ é opcional (vendedor pessoa física).
        @Pattern(regexp = "^(\\d{14})?$", message = "CNPJ deve ter 14 dígitos, somente números")
        String cnpj,

        @NotBlank(message = "E-mail de contato é obrigatório")
        @Email(message = "E-mail de contato inválido")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
        String emailContato,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        // Reaproveita o mesmo DTO do Endereço (CEP + número...). @Valid faz o Spring
        // validar também os campos de DENTRO dele; erros aparecem como "endereco.cep".
        @NotNull(message = "Endereço é obrigatório")
        @Valid
        EnderecoRequest endereco
) {
}
