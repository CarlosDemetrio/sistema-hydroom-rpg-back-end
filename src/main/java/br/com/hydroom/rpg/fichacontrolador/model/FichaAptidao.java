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
 * Entidade que armazena os valores das aptidões de uma ficha.
 */
@Entity
@Table(name = "ficha_aptidoes", indexes = {
    @Index(name = "idx_ficha_aptidao_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_aptidao_config", columnList = "aptidao_config_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_aptidao", columnNames = {"ficha_id", "aptidao_config_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaAptidao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaAptidao.FICHA_OBRIGATORIA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @NotNull(message = ValidationMessages.FichaAptidao.APTIDAO_CONFIG_OBRIGATORIA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aptidao_config_id", nullable = false)
    private AptidaoConfig aptidaoConfig;

    @NotNull
    @Builder.Default
    @Column(name = "base", nullable = false)
    private Integer base = 0;

    @NotNull
    @Builder.Default
    @Column(name = "sorte", nullable = false)
    private Integer sorte = 0;

    @NotNull
    @Builder.Default
    @Column(name = "classe", nullable = false)
    private Integer classe = 0;

    /**
     * Total calculado: base + sorte + classe.
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
                     (sorte != null ? sorte : 0) +
                     (classe != null ? classe : 0);
    }
}
