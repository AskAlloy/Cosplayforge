package com.horizon.Cosplayforge.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "Loja")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)


public class Loja {

	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @EqualsAndHashCode.Include
	 private Long id;
	 
	 @OneToOne
	    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
	    private Usuario proprietario;
	 
	    @Column(nullable = false, length = 150)
	    private String nome;
	 
	    @Lob
	    private String descricao;
	 
	    // O cnpj vai ser opcional.
	    @Column(unique = true, length = 14)
	    private String cnpj;
	 
	    @Column(name = "email_contato", nullable = false, length = 150)
	    private String emailContato;
	 
	    @Column(length = 20)
	    private String telefone;
	    
	    @OneToOne(cascade = CascadeType.ALL)
	    @JoinColumn(name = "endereco_id", nullable = false)
	    private Endereco endereco;
	 
	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false, length = 20)
	    @Builder.Default
	    private StatusLoja status = StatusLoja.EM_ANALISE;
	 
	    @Column(name = "data_cadastro", nullable = false, updatable = false)
	    private LocalDateTime dataCadastro;
	 
	    @Column(name = "data_atualizacao", nullable = false)
	    private LocalDateTime dataAtualizacao;
	 
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
}
