package com.horizon.Cosplayforge.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.horizon.Cosplayforge.model.Loja;
import com.horizon.Cosplayforge.model.StatusLoja;

public interface LojaRepository  extends JpaRepository<Loja, Long>{
	
	Optional<Loja>findByProprietarioId(UUID usuarioId);
	
	boolean existsByCnpj(String cnpj);

	// para não acusar duplicidade quando atualizar a loja com cnpj
	boolean existsByCnpjAndIdNot(String cnpj, Long id);

	List<Loja> findByStatus(StatusLoja status, Sort sort);

}
