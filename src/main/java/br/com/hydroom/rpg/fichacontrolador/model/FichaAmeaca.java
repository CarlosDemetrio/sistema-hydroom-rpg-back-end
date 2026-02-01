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
import org.hibernate.envers.Audited;

/**
 * Entidade que armazena os dados de ameaça de uma ficha.
 */
@Entity
@Table(name = "ficha_ameaca", indexes = {
    @Index(name = "idx_ficha_ameaca_ficha", columnList = "ficha_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_ameaca", columnNames = {"ficha_id"})
})
@Audited
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaAmeaca extends AuditableEntity {

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
    @Column(name = "valor_itens", nullable = false)
    private Integer valorItens = 0;

    @NotNull
    @Min(value = 0, message = ValidationMessages.FichaAmeaca.TITULOS_MINIMO)
    @Builder.Default
    @Column(name = "valor_titulos", nullable = false)
    private Integer valorTitulos = 0;

    /**
     * Calcula a ameaça total.
     * Total = Base (calculado pela fórmula) + Itens + Títulos
     * Base = (Fórmula configurada em AmeacaConfig)
     */
    public Integer getValorModificadores() {
        return (valorItens != null ? valorItens : 0) +
               (valorTitulos != null ? valorTitulos : 0);
    }
}
