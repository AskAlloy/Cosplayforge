package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.Pedido;
import com.horizon.Cosplayforge.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        Long id,
        StatusPedido status,
        Long lojaId,
        String lojaNome,
        UUID clienteId,
        String clienteNome,
        List<ItemPedidoResponse> itens,
        BigDecimal valorProdutos,
        BigDecimal valorFrete,
        BigDecimal valorTotal,
        String freteServico,
        Integer fretePrazoDias,
        String codigoRastreio,
        EnderecoResponse enderecoEntrega,
        LocalDateTime dataCriacao
) {
    public static PedidoResponse de(Pedido p) {
        return new PedidoResponse(
                p.getId(), p.getStatus(),
                p.getLoja().getId(), p.getLoja().getNome(),
                p.getCliente().getId(), p.getCliente().getNome(),
                p.getItens().stream().map(ItemPedidoResponse::de).toList(),
                p.getValorProdutos(), p.getValorFrete(), p.getValorTotal(),
                p.getFreteServico(), p.getFretePrazoDias(), p.getCodigoRastreio(),
                EnderecoResponse.de(p.getEnderecoEntrega()), p.getDataCriacao());
    }
}
