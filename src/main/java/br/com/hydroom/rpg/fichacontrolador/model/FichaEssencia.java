package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidade que armazena os dados de essência de uma ficha.
 */
@Entity
@Table(name = "ficha_essencia", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_essencia", columnNames = {"ficha_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaEssencia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaEssencia.FICHA_OBRIGATORIA)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false, unique = true)
    private Ficha ficha;

    @NotNull
    @Min(value = 0)
    @Builder.Default
    @Column(name = "renascimentos", nullable = false)
    private Integer renascimentos = 0;

    @NotNull
    @Min(value = 0)
    @Builder.Default
    @Column(name = "vantagens", nullable = false)
    private Integer vantagens = 0;

    @NotNull
    @Min(value = 0)
    @Builder.Default
    @Column(name = "outros", nullable = false)
    private Integer outros = 0;

    /**
     * Total calculado: renascimentos + vantagens + outros.
     */
    @NotNull
    @Builder.Default
    @Column(name = "total", nullable = false)
    private Integer total = 0;

    /**
     * Essência atual restante do personagem (estado de combate/uso de magias).
     * Inicializada com total ao criar a ficha.
     * Atualizada via PUT /fichas/{id}/vida.
     */
    @NotNull
    @Builder.Default
    @Column(name = "essencia_atual", nullable = false)
    private Integer essenciaAtual = 0;

    /**
     * Recalcula e persiste o total.
     */
    public void recalcularTotal() {
        this.total = (renascimentos != null ? renascimentos : 0) +
                     (vantagens != null ? vantagens : 0) +
                     (outros != null ? outros : 0);
    }
}
