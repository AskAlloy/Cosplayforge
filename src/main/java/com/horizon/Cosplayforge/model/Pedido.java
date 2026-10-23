package com.horizon.Cosplayforge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Pedido")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;
 
    @ManyToOne
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusPedido status = StatusPedido.PENDENTE;
 
    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
 
    // RN008: valorTotal = valorProdutos + valorFrete
    @Column(name = "valor_produtos", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorProdutos;
 
    @Column(name = "valor_frete", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFrete;
 
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;
 
    // Frete escolhida
    // retorno da API externa não pode ser consultado depois.
    @Column(name = "frete_servico", nullable = false, length = 100)
    private String freteServico;
 
    @Column(name = "frete_prazo_dias", nullable = false)
    private Integer fretePrazoDias;
 
    
    @Column(name = "codigo_rastreio", length = 50)
    private String codigoRastreio;
 
    @ManyToOne
    @JoinColumn(name = "endereco_entrega_id", nullable = false)
    private Endereco enderecoEntrega;
 
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

    /**
      Muda o status somente se a transição for válida.
     */
    public void alterarStatus(StatusPedido novoStatus) {
        if (!status.podeIrPara(novoStatus)) {
            throw new IllegalStateException(
                    "Transição inválida de status: " + status + " -> " + novoStatus);
        }
        this.status = novoStatus;
    }

}
