package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
    @Column(name = "valor_base", nullable = false)
    private Integer valorBase = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_nivel", nullable = false)
    private Integer valorNivel = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_outros", nullable = false)
    private Integer valorOutros = 0;

    /**
     * Calcula o valor total do atributo.
     * Total = Base + Nível + Outros
     */
    public Integer getValorTotal() {
        return (valorBase != null ? valorBase : 0) +
               (valorNivel != null ? valorNivel : 0) +
               (valorOutros != null ? valorOutros : 0);
    }
}
