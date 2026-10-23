package com.horizon.Cosplayforge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.*;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "ItemCarrinho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemCarrinho {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne
    @JoinColumn(name = "carrinho_id", nullable = false)
    private Carrinho carrinho;
 
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto; 
 
    @Column(nullable = false)
    private Integer quantidade;
 
    @Column(name = "data_adicionado", nullable = false, updatable = false)
    private LocalDateTime dataAdicionado;
 
    @PrePersist
    protected void aoCriar() {
        this.dataAdicionado = LocalDateTime.now();
    }
}