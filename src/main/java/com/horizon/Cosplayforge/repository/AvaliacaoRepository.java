package com.horizon.Cosplayforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.horizon.Cosplayforge.model.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
	 
    // Para evitar avaliação duplicada do mesmo produto no mesmo pedido
    boolean existsByPedidoIdAndProdutoId(Long pedidoId, Long produtoId);
 
    // Para evitar avaliação duplicada da loja no mesmo pedido 
    boolean existsByPedidoIdAndProdutoIsNull(Long pedidoId);
 
    List<Avaliacao> findByProdutoIdOrderByDataAvaliacaoDesc(Long produtoId);
 
    @Query("SELECT a FROM Avaliacao a WHERE a.produto IS NULL AND a.pedido.loja.id = :lojaId ORDER BY a.dataAvaliacao DESC")
    List<Avaliacao> findAvaliacoesDaLoja(@Param("lojaId") Long lojaId);
 
    // Médias retornam null quando ainda não há avaliações
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.produto.id = :produtoId")
    Double mediaNotaDoProduto(@Param("produtoId") Long produtoId);
 
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.produto IS NULL AND a.pedido.loja.id = :lojaId")
    Double mediaNotaDaLoja(@Param("lojaId") Long lojaId);
}
