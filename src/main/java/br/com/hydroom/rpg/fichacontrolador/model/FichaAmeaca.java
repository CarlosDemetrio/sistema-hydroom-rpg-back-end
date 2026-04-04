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
 * Entidade que armazena os dados de ameaça de uma ficha.
 */
@Entity
@Table(name = "ficha_ameaca", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_ameaca", columnNames = {"ficha_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaAmeaca extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.FichaAmeaca.FICHA_OBRIGATORIA)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false, unique = true)
    private Ficha ficha;

    @NotNull
    @Min(value = 0, message = ValidationMessages.FichaAmeaca.ITENS_MINIMO)
    @Builder.Default
    @Column(name = "itens", nullable = false)
    private Integer itens = 0;

    @NotNull
    @Min(value = 0, message = ValidationMessages.FichaAmeaca.TITULOS_MINIMO)
    @Builder.Default
    @Column(name = "titulos", nullable = false)
    private Integer titulos = 0;

    @NotNull
    @Min(value = 0)
    @Builder.Default
    @Column(name = "renascimentos", nullable = false)
    private Integer renascimentos = 0;

    @NotNull
    @Min(value = 0)
    @Builder.Default
    @Column(name = "outros", nullable = false)
    private Integer outros = 0;

    /**
     * Total calculado: ficha.nivel + itens + titulos + renascimentos + outros.
     * Calculado exclusivamente via FichaCalculationService.calcularAmeacaTotal().
     * O nivel da ficha é externo a esta entidade e não pode ser calculado aqui.
     */
    @NotNull
    @Builder.Default
    @Column(name = "total", nullable = false)
    private Integer total = 0;
}
