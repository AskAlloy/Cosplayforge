package com.horizon.Cosplayforge.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {
	@Id
	    @GeneratedValue(strategy = GenerationType.UUID)
	    @Column(name = "id", updatable = false, nullable = false)
		@EqualsAndHashCode.Include
		private UUID id;

	    @Column(name = "nome_real", nullable = false, length = 100)
	    private String nome;
	    
	    @Column(name = "nome_usuario", nullable = false, unique = true, length = 50)
	    private String nomeUsuario;

	    @Column(nullable = false, unique = true, length = 180)
	    private String email;
	    
	    @Column(name = "email_confirmado", nullable = false)
	    private Boolean emailConfirmado = false;

	    @Column(name = "token_confirmacao_email", length = 100)
	    private String tokenConfirmacaoEmail;

	    @Column(name = "senha_hash", nullable = false, length = 255)
	    private String senhaHash;
	    
	    @Column(name = "usuario_cpf", nullable = false, unique = true, length = 11 )
	    private String cpf;


	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false, length = 20)
	    private PerfilUsuario perfil;
	    
	    @Column(length = 20)
	    private String telefone;
	    
	    @Column(name = "data_cadastro", nullable = false, updatable = false)
	    private LocalDateTime dataCadastro;
	 
	    @Column(name = "data_atualizacao", nullable = false)
	    private LocalDateTime dataAtualizacao;
	 
	    // definitivamente o registro 7 dias após a exclusão.
	    @Column(name = "data_exclusao")
	    private LocalDateTime dataExclusao;
	 
	    @PrePersist
	    protected void aoCriar() {
	        LocalDateTime agora = LocalDateTime.now();
	        this.dataCadastro = agora;
	        this.dataAtualizacao = agora;
	    }
	 
	    @PreUpdate
	    protected void aoAtualizar() {
	        this.dataAtualizacao = LocalDateTime.now();
	    }
	 
	    public boolean estaInativo() {
	        return dataExclusao != null;
	    }
}
