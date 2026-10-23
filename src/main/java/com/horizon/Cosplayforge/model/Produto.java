package com.horizon.Cosplayforge.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "Produto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class Produto {

	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @EqualsAndHashCode.Include
	 private Long id;
	 

	@ManyToOne
	@JoinColumn(name = "loja_id", nullable = false)
	private Loja loja;
	 
	@ManyToOne
	@JoinColumn(name = "categoria_id", nullable = false)
	private Categoria categoria; 
	 
	@Column(nullable = false, length = 150)
	private String nome;
	 
	@Lob
	@Column(nullable = false)
	private String descricao;
	 
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal preco;
	 
	@Column(name = "quantidade_estoque", nullable = false)
	private Integer quantidadeEstoque;
	
	@Column(name="imagem_produto", length = 1000)
	private String imagem;
	 
	
	@Column(nullable = false)
	@Builder.Default
	private Boolean personalizavel = false;
	
	@Column(name = "peso_kg", nullable = false, precision = 6, scale = 3)
	private BigDecimal pesoKg;

	@Column(name = "altura_cm", nullable = false)
	private Integer alturaCm;

	@Column(name = "largura_cm", nullable = false)
	private Integer larguraCm;

	@Column(name = "comprimento_cm", nullable = false)
	private Integer comprimentoCm;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	@Builder.Default
	private StatusProduto status = StatusProduto.ATIVO;
	
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
 
    /**
     *Verificar se tem disponivel
     */
    public boolean estaDisponivel() {
        return status == StatusProduto.ATIVO && quantidadeEstoque != null && quantidadeEstoque > 0;
    }
}
