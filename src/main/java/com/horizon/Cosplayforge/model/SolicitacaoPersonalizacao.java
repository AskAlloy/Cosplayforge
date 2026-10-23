package com.horizon.Cosplayforge.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SolicitacaoPersonalizacao")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class SolicitacaoPersonalizacao {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;
 
    // Produto-base que o cliente quer personalizar.
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;
 
    @Column(nullable = false, length = 2000)
    private String descricao;
 
    @Column(length = 50)
    private String tamanho;
 
    @Column(length = 50)
    private String cor;
 
    @Column(length = 100)
    private String material;
 
    @Column(length = 100)
    private String acabamento;
 
    @Column(length = 1000)
    private String observacoes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusSolicitacao status = StatusSolicitacao.ENVIADA;
 
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
 
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;
 
    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        this.dataCriacao = agora;
        this.dataAtualizacao = agora;
    }
 
    @PreUpdate
    protected void aoAtualizar() {
        this.dataAtualizacao = LocalDateTime.now();
    }

}
