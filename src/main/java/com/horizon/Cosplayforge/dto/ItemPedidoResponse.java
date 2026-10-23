package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(Long produtoId, String produtoNome, Integer quantidade,
                                 BigDecimal precoUnitario, BigDecimal subtotal) {

    public static ItemPedidoResponse de(ItemPedido i) {
        return new ItemPedidoResponse(i.getProduto().getId(), i.getProduto().getNome(),
                i.getQuantidade(), i.getPrecoUnitario(), i.calcularSubtotal());
    }
}