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
 * Entidade que armazena os valores dos bônus de uma ficha.
 */
@Entity
@Table(name = "ficha_bonus", indexes = {
    @Index(name = "idx_ficha_bonus_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_bonus_config", columnList = "bonus_config_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_bonus", columnNames = {"ficha_id", "bonus_config_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaBonus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaBonus.FICHA_OBRIGATORIA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @NotNull(message = ValidationMessages.FichaBonus.BONUS_CONFIG_OBRIGATORIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_config_id", nullable = false)
    private BonusConfig bonusConfig;

    /**
     * Valor base calculado via formulaBase do BonusConfig.
     */
    @NotNull
    @Builder.Default
    @Column(name = "base", nullable = false)
    private Integer base = 0;

    @NotNull
    @Builder.Default
    @Column(name = "vantagens", nullable = false)
    private Integer vantagens = 0;

    @NotNull
    @Builder.Default
    @Column(name = "classe", nullable = false)
    private Integer classe = 0;

    @NotNull
    @Builder.Default
    @Column(name = "itens", nullable = false)
    private Integer itens = 0;

    @NotNull
    @Builder.Default
    @Column(name = "gloria", nullable = false)
    private Integer gloria = 0;

    @NotNull
    @Builder.Default
    @Column(name = "outros", nullable = false)
    private Integer outros = 0;

    /**
     * Total calculado: base + vantagens + classe + itens + gloria + outros.
     */
    @NotNull
    @Builder.Default
    @Column(name = "total", nullable = false)
    private Integer total = 0;

    /**
     * Recalcula e persiste o total.
     */
    public void recalcularTotal() {
        this.total = (base != null ? base : 0) +
                     (vantagens != null ? vantagens : 0) +
                     (classe != null ? classe : 0) +
                     (itens != null ? itens : 0) +
                     (gloria != null ? gloria : 0) +
                     (outros != null ? outros : 0);
    }
}
