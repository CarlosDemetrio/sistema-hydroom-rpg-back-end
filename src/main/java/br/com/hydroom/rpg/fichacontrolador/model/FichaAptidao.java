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
 * Entidade que armazena os valores das aptidões de uma ficha.
 */
@Entity
@Table(name = "ficha_aptidoes", indexes = {
    @Index(name = "idx_ficha_aptidao_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_aptidao_config", columnList = "aptidao_config_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_aptidao", columnNames = {"ficha_id", "aptidao_config_id"})
})
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
    @Column(name = "valor_base", nullable = false)
    private Integer valorBase = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_sorte", nullable = false)
    private Integer valorSorte = 0;

    @NotNull
    @Builder.Default
    @Column(name = "valor_classe", nullable = false)
    private Integer valorClasse = 0;

    /**
     * Calcula o valor total da aptidão.
     * Total = Base + Sorte + Classe
     */
    public Integer getValorTotal() {
        return (valorBase != null ? valorBase : 0) +
               (valorSorte != null ? valorSorte : 0) +
               (valorClasse != null ? valorClasse : 0);
    }
}
