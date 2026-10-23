package com.horizon.Cosplayforge.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
        String descricao,

        @NotNull(message = "Categoria é obrigatória")
        Long categoriaId,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "Preço deve ter até 2 casas decimais")
        BigDecimal preco,

        @Min(value = 0, message = "Estoque não pode ser negativo")
        Integer quantidadeEstoque,

        @Size(max = 1000, message = "URL da imagem deve ter no máximo 1000 caracteres")
        @Pattern(regexp = "^(https?://.+)?$", message = "Imagem deve ser uma URL http(s)")
        String imagem,

        Boolean personalizavel,

        // Peso e medidas alimentam o cálculo de frete
        @NotNull(message = "Peso é obrigatório")
        @DecimalMin(value = "0.001", message = "Peso deve ser maior que zero")
        @Digits(integer = 3, fraction = 3, message = "Peso deve ter até 3 casas decimais")
        BigDecimal pesoKg,

        @NotNull(message = "Altura é obrigatória")
        @Min(value = 1, message = "Altura deve ser de pelo menos 1 cm")
        Integer alturaCm,

        @NotNull(message = "Largura é obrigatória")
        @Min(value = 1, message = "Largura deve ser de pelo menos 1 cm")
        Integer larguraCm,

        @NotNull(message = "Comprimento é obrigatório")
        @Min(value = 1, message = "Comprimento deve ser de pelo menos 1 cm")
        Integer comprimentoCm

) {
}
