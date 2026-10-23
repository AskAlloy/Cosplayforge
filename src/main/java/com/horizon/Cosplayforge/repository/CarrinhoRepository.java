package com.horizon.Cosplayforge.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.horizon.Cosplayforge.model.Carrinho;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long>{

	Optional<Carrinho> findByUsuarioId(UUID usuarioId);
	
}
