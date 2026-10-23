package com.horizon.Cosplayforge.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "Orcamento")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Orcamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "solicidacao_id",nullable = false, unique = true)
	private SolicitacaoPersonalizacao solicitacao;
	
	@Column(nullable = false, precision = 10,scale = 2)
	private BigDecimal valor;
	
	@Column(name ="prazo_dias",nullable = false)
	private Integer prazoDias;
	
	@Column
	private String descricao;
	
	@Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusOrcamento status = StatusOrcamento.RASCUNHO;
	
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
