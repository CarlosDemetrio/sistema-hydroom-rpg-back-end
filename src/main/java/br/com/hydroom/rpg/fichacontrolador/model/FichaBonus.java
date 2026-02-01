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
 * Entidade que armazena os valores dos bônus de uma ficha.
 */
@Entity
@Table(name = "ficha_bonus", indexes = {
    @Index(name = "idx_ficha_bonus_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_bonus_config", columnList = "bonus_config_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_bonus", columnNames = {"ficha_id", "bonus_config_id"})
})
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

    @NotNull
    @Builder.Default
    @Column(name = "valor_vantagens", nullable = false)
    private Integer valorVantagens = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_classe", nullable = false)
    private Integer valorClasse = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_itens", nullable = false)
    private Integer valorItens = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_gloria", nullable = false)
    private Integer valorGloria = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_outros", nullable = false)
    private Integer valorOutros = 0;

    /**
     * Calcula o valor total do bônus.
     * Total = Base (calculado pela fórmula) + Vantagens + Classe + Itens + Glória + Outros
     * Nota: o valor base deve ser calculado pela fórmula definida em BonusConfig
     */
    public Integer getValorModificadores() {
        return (valorVantagens != null ? valorVantagens : 0) +
               (valorClasse != null ? valorClasse : 0) +
               (valorItens != null ? valorItens : 0) +
               (valorGloria != null ? valorGloria : 0) +
               (valorOutros != null ? valorOutros : 0);
    }
}
