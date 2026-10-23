package com.horizon.Cosplayforge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.horizon.Cosplayforge.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

   boolean existsByNomeIgnoreCase(String nome);

   Optional<Categoria> findByNomeIgnoreCase(String nome);
}