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
 * Entidade que armazena os valores dos atributos de uma ficha.
 */
@Entity
@Table(name = "ficha_atributos", indexes = {
    @Index(name = "idx_ficha_atributo_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_atributo_config", columnList = "atributo_config_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_atributo", columnNames = {"ficha_id", "atributo_config_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaAtributo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaAtributo.FICHA_OBRIGATORIA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @NotNull(message = ValidationMessages.FichaAtributo.ATRIBUTO_CONFIG_OBRIGATORIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_config_id", nullable = false)
    private AtributoConfig atributoConfig;

    @NotNull
    @Builder.Default
    @Column(name = "base", nullable = false)
    private Integer base = 0;

    @NotNull
    @Builder.Default
    @Column(name = "nivel", nullable = false)
    private Integer nivel = 0;

    @NotNull
    @Builder.Default
    @Column(name = "outros", nullable = false)
    private Integer outros = 0;

    /**
     * Total calculado: base + nivel + outros.
     */
    @NotNull
    @Builder.Default
    @Column(name = "total", nullable = false)
    private Integer total = 0;

    /**
     * Ímpeto calculado via formulaImpeto do AtributoConfig.
     */
    @Builder.Default
    @Column(name = "impeto")
    private Double impeto = 0.0;

    /**
     * Recalcula e persiste o total.
     */
    public void recalcularTotal() {
        this.total = (base != null ? base : 0) +
                     (nivel != null ? nivel : 0) +
                     (outros != null ? outros : 0);
    }
}
