package com.horizon.Cosplayforge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "ItemPedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemPedido {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
 
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;
 
    @Column(nullable = false)
    private Integer quantidade;
 
    // Preço do produto NO MOMENTO da compra. Se o vendedor alterar o preço
    // depois, pedidos antigos não podem mudar de valor.
    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;
 
    /**
     * A soma dos subtotais forma o valorProdutos do Pedido (RN008).
     */
    public BigDecimal calcularSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}