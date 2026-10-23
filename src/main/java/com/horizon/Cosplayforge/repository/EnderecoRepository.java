package com.horizon.Cosplayforge.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
 
import org.springframework.data.jpa.repository.JpaRepository;
 
import com.horizon.Cosplayforge.model.Endereco;
 
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
 
    List<Endereco> findByUsuarioId(UUID usuarioId);
 
    // Garante que o cliente só use endereço que é dele
    Optional<Endereco> findByIdAndUsuarioId(Long id, UUID usuarioId);
}

