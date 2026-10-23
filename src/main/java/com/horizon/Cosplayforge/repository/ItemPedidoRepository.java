package com.horizon.Cosplayforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
 
import com.horizon.Cosplayforge.model.ItemPedido;
 
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
 
    List<ItemPedido> findByPedidoId(Long pedidoId);
 
    // Produto com pedido vinculado deve ser inativado, não excluído
    boolean existsByProdutoId(Long produtoId);

}
