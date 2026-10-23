package com.horizon.Cosplayforge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.*;

@Entity
@Table(name = "Endereco")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)


public class Endereco {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
	private Long id;
	
	// Endereço de origem dos envios (RF009 - Calcular Frete).
    @Column(nullable = false, length = 8)
    private String cep;
 
    @Column(nullable = false, length = 150)
    private String logradouro;
 
    @Column(nullable = false, length = 20)
    private String numero;
 
    @Column(length = 100)
    private String complemento;
 
    @Column(nullable = false, length = 100)
    private String bairro;
 
    @Column(nullable = false, length = 100)
    private String cidade;
 
    @Column(nullable = false, length = 2)
    private String uf;
    
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")   // antes era user_id
    private Usuario usuario;

}
