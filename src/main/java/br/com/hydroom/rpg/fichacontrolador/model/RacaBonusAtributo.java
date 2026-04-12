package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Relacionamento N:N entre Raça e Atributo para definir bônus raciais.
 *
 * <p>Exemplo: Elfo tem +2 em Agilidade e -1 em Vigor</p>
 *
 * @author Carlos Demétrio
 * @since 2026-02-05
 */
@Entity
@Table(name = "raca_bonus_atributos",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_raca_bonus_atributo", columnNames = {"raca_id", "atributo_id"})
    },
    indexes = {
        @Index(name = "idx_raca_bonus_raca", columnList = "raca_id"),
        @Index(name = "idx_raca_bonus_atributo", columnList = "atributo_id")
    }
)
@Data
@EqualsAndHashCode(callSuper = true, exclude = "raca")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RacaBonusAtributo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id", nullable = false)
    private Raca raca;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_id", nullable = false)
    private AtributoConfig atributo;

    /**
     * Valor do bônus (pode ser negativo para penalidade).
     * Exemplo: +2, -1, +3, etc.
     */
    @NotNull
    @Column(nullable = false)
    private Integer bonus;
}
