package com.horizon.Cosplayforge.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.horizon.Cosplayforge.model.SolicitacaoPersonalizacao;

public interface SolicitacaoPersonalizacaoRepository extends JpaRepository<SolicitacaoPersonalizacao, Long> {

   // Solicitações do cliente
   List<SolicitacaoPersonalizacao> findByClienteIdOrderByDataCriacaoDesc(UUID clienteId);

   Optional<SolicitacaoPersonalizacao> findByIdAndClienteId(Long id, UUID clienteId);

   // Solicitações destinadas à loja do vendedor
   @Query("SELECT s FROM SolicitacaoPersonalizacao s WHERE s.produto.loja.id = :lojaId ORDER BY s.dataCriacao DESC")
   List<SolicitacaoPersonalizacao> findDaLoja(@Param("lojaId") Long lojaId);

   // Vendedor só enxerga solicitação da própria loja
   @Query("SELECT s FROM SolicitacaoPersonalizacao s WHERE s.id = :id AND s.produto.loja.id = :lojaId")
   Optional<SolicitacaoPersonalizacao> findByIdAndLojaId(@Param("id") Long id, @Param("lojaId") Long lojaId);
}
