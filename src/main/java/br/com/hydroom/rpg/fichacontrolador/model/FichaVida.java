package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidade que armazena os dados de vida de uma ficha.
 */
@Entity
@Table(name = "ficha_vida", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_vida", columnNames = {"ficha_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaVida extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaVida.FICHA_OBRIGATORIA)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false, unique = true)
    private Ficha ficha;

    /**
     * Vigor total (calculado via atributo VIG).
     */
    @NotNull
    @Builder.Default
    @Column(name = "vt", nullable = false)
    private Integer vt = 0;

    @NotNull
    @Builder.Default
    @Column(name = "outros", nullable = false)
    private Integer outros = 0;

    /**
     * Vida total calculada: vigorTotal + ficha.nivel + vt + ficha.renascimentos + outros.
     * Calculada exclusivamente via FichaCalculationService.calcularVidaTotal().
     * O vigorTotal e o nivel da ficha são externos a esta entidade e não podem ser calculados aqui.
     */
    @NotNull
    @Builder.Default
    @Column(name = "vida_total", nullable = false)
    private Integer vidaTotal = 0;

    /**
     * Vida atual restante do personagem (estado de combate).
     * Inicializada com vidaTotal ao criar a ficha.
     * Atualizada via PUT /fichas/{id}/vida.
     */
    @NotNull
    @Builder.Default
    @Column(name = "vida_atual", nullable = false)
    private Integer vidaAtual = 0;
}
