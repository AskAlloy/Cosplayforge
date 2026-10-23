package com.horizon.Cosplayforge.repository;

import com.horizon.Cosplayforge.model.Produto;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProdutoRepository extends JpaRepository<Produto, Long>{
	
	Page<Produto> findByLojaId(Long lojaId, Pageable pageable);
	
	Optional<Produto> findByIdAndLojaId(Long Id, Long lojaId);
	
	boolean existsByCategoriaId(Long categoriaId);
	
	@Query("""
            SELECT p FROM Produto p
            WHERE p.status = com.horizon.Cosplayforge.model.StatusProduto.ATIVO
              AND p.loja.status = com.horizon.Cosplayforge.model.StatusLoja.ATIVA
              AND (:termo IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')))
              AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
              AND (:precoMin IS NULL OR p.preco >= :precoMin)
              AND (:precoMax IS NULL OR p.preco <= :precoMax)
            """)
    Page<Produto> pesquisar(@Param("termo") String termo,
                            @Param("categoriaId") Long categoriaId,
                            @Param("precoMin") BigDecimal precoMin,
                            @Param("precoMax") BigDecimal precoMax,
                            Pageable pageable);
	
	// Para falar da baixa de estoque
	// Evita que dois clientes comprem a última unidade ao mesmo tempo. 
	@Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Produto p
            SET p.quantidadeEstoque = p.quantidadeEstoque - :quantidade
            WHERE p.id = :id AND p.quantidadeEstoque >= :quantidade
            """)
    int baixarEstoque(@Param("id") Long id, @Param("quantidade") int quantidade);

	@Modifying(flushAutomatically = true)
	@Query("""
            UPDATE Produto p
            SET p.quantidadeEstoque = p.quantidadeEstoque + :quantidade
            WHERE p.id = :id
            """)
	int devolverEstoque(@Param("id") Long id, @Param("quantidade") int quantidade);

    Long categoriaId(Long categoriaId);
}
