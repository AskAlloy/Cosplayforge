package com.horizon.Cosplayforge.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarrinhoResponse(List<GrupoLoja>lojas, BigDecimal total) {
    public record GrupoLoja(Long lojaId, String lojaNome, List<Item> itens, BigDecimal subtotal) {
    }

    // "disponivel" = produto ativo, loja ativa e estoque suficiente)
    public record Item(Long produtoId, String nome, String imagem, BigDecimal precoUnitario,
                       Integer quantidade, BigDecimal subtotal, boolean disponivel, Integer estoque)
    {

    }
}
