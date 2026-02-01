package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Tabela de progressão de níveis - XP necessária por nível.
 * Configurável pelo Mestre.
 */
@Entity
@Table(name = "niveis_config", uniqueConstraints = {
        @UniqueConstraint(name = "uk_nivel_jogo", columnNames = {"jogo_id", "nivel"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class NivelConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    @NotNull(message = "Jogo é obrigatório")
    private Jogo jogo;

    @Column(nullable = false)
    @NotNull(message = "Nível é obrigatório")
    @Min(value = 1, message = "Nível deve ser maior que zero")
    private Integer nivel;

    @Column(name = "xp_necessaria", nullable = false)
    @NotNull(message = "XP necessária é obrigatória")
    @Min(value = 0, message = "XP necessária não pode ser negativa")
    private Long xpNecessaria;

    /**
     * Pontos de atributo ganhos ao atingir este nível.
     * Padrão: 3 pontos por nível.
     */
    @Column(name = "pontos_atributo", nullable = false)
    @NotNull(message = "Pontos de atributo são obrigatórios")
    @Min(value = 0, message = "Pontos de atributo não podem ser negativos")
    private Integer pontosAtributo = 3;

    /**
     * Valor máximo que atributos podem ter neste nível (limitador).
     */
    @Column(name = "limitador_atributo", nullable = false)
    @NotNull(message = "Limitador de atributo é obrigatório")
    @Min(value = 1, message = "Limitador de atributo deve ser maior que zero")
    private Integer limitadorAtributo;
}
