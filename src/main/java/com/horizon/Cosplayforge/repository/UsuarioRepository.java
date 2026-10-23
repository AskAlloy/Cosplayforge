package com.horizon.Cosplayforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.horizon.Cosplayforge.model.Usuario;
import com.horizon.Cosplayforge.model.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>{

  Optional<Usuario> findByEmailIgnoreCase(String email);

  //login por email ou nome de Usuario
    @Query("""
            SELECT u FROM Usuario u
            WHERE LOWER(u.email) = LOWER(:login)
               OR LOWER(u.nomeUsuario) = LOWER(:login)
            """)
    Optional<Usuario> buscarParaLogin(@Param("login") String login);

    // token de confirmar o email
    Optional<Usuario> findByTokenConfirmacaoEmail(String token);
    
    //Ver se tem antes de cadastrar
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNomeUsuario(String nomeUsuario);
    boolean existsByCpf(String cpf);
 
    //(admin): listagem por perfil
    List<Usuario> findByPerfil(PerfilUsuario perfil);

    //quantos usuarios ATIVOS tem nesse perfil (Cliente,Vendedor,ADM)
    long countByPerfilAndDataExclusaoIsNull(PerfilUsuario perfil);

    //contas inativadas há mais de 7 dias, para remoção definitiva
    List<Usuario> findByDataExclusaoBefore(LocalDateTime limite);
 
}
