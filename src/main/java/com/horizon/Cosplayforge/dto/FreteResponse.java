package com.horizon.Cosplayforge.dto;

import java.math.BigDecimal;
import java.util.List;

public record FreteResponse(List<LojaFrete> lojas) {
    public record LojaFrete(
            Long lojaId,
            String lojaNome,
            List<Opcao> opcoes){}

    public record Opcao(
            Integer servicoId,
            String servico,
            BigDecimal preco,
            Integer prazoDias){}
}
