package com.horizon.Cosplayforge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
 
import com.horizon.Cosplayforge.model.ItemCarrinho;

public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho, Long> {

	List<ItemCarrinho> findByCarrinhoId(Long carrinhoId);
	
	Optional<ItemCarrinho> findByCarrinhoIdAndProdutoId(Long carrinhoId, Long produtoId);

	void deleteByProdutoId(Long produtoId);

	void deleteByCarrinhoId(Long carrinhoId);
}
