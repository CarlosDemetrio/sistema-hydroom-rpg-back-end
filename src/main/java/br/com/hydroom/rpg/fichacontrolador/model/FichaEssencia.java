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

/**
 * Entidade que armazena os dados de essência de uma ficha.
 */
@Entity
@Table(name = "ficha_essencia", indexes = {
    @Index(name = "idx_ficha_essencia_ficha", columnList = "ficha_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_essencia", columnNames = {"ficha_id"})
})
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
    @Min(value = 0, message = ValidationMessages.FichaEssencia.GASTO_MINIMO)
    @Builder.Default
    @Column(name = "gasto_temporario", nullable = false)
    private Integer gastoTemporario = 0;

    /**
     * Calcula a essência disponível.
     * Disponível = Total (calculado pela fórmula) - GastoTemporario
     * Total = (Fórmula configurada em EssenciaConfig)
     */
    public Integer calcularDisponivel(Integer essenciaTotal) {
        int total = essenciaTotal != null ? essenciaTotal : 0;
        int gasto = gastoTemporario != null ? gastoTemporario : 0;
        return Math.max(0, total - gasto);
    }
}
