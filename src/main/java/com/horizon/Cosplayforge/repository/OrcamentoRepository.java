package com.horizon.Cosplayforge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
 
import com.horizon.Cosplayforge.model.Orcamento;
 
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
 
    Optional<Orcamento> findBySolicitacaoId(Long solicitacaoId);
 
    boolean existsBySolicitacaoId(Long solicitacaoId);
}
