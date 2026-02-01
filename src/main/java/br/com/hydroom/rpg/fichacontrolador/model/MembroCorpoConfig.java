package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Configuração dos membros do corpo para sistema de vida.
 */
@Entity
@Table(name = "membro_corpo_config", indexes = {
    @Index(name = "idx_membro_corpo_jogo", columnList = "jogo_id, ativo")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_membro_corpo_jogo_nome", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroCorpoConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nome;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "1.00")
    @Column(name = "porcentagem_vida", nullable = false, precision = 3, scale = 2)
    private BigDecimal porcentagemVida;

    @Builder.Default
    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;
}
