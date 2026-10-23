package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.Produto;
import com.horizon.Cosplayforge.model.StatusProduto;

import java.math.BigDecimal;

public record ProdutoResponse(

        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidadeEstoque,
        boolean disponivel, // ATIVO e com estoque > 0
        String imagem,
        boolean personalizavel,
        BigDecimal pesoKg,
        Integer alturaCm,
        Integer larguraCm,
        Integer comprimentoCm,
        StatusProduto status,
        Long categoriaId,
        String categoriaNome,
        Long lojaId,
        String lojaNome

) {
    public static ProdutoResponse de(Produto p){
     return new ProdutoResponse(
            p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getQuantidadeEstoque(),
                p.estaDisponivel(), p.getImagem(), Boolean.TRUE.equals(p.getPersonalizavel()),
                        p.getPesoKg(), p.getAlturaCm(), p.getLarguraCm(), p.getComprimentoCm(), p.getStatus(),
                        p.getCategoria().getId(), p.getCategoria().getNome(),
                p.getLoja().getId(), p.getLoja().getNome());
    }
}
