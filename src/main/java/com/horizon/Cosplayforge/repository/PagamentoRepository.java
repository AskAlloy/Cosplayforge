package com.horizon.Cosplayforge.repository; 
import java.util.List;
import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
 
import com.horizon.Cosplayforge.model.Pagamento;
 
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
 
    // Histórico de tentativas do pedido
    List<Pagamento> findByPedidoIdOrderByDataCriacaoDesc(Long pedidoId);
 
    //Mercado Pago identifica a transação por este id
    Optional<Pagamento> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);
}
