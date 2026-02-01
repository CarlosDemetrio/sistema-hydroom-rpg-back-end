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
 * Configuração de pontos de vantagem ganhos por nível.
 * Configurável pelo Mestre.
 */
@Entity
@Table(name = "pontos_vantagem_config", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pontos_vantagem_nivel_jogo", columnNames = {"jogo_id", "nivel"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class PontosVantagemConfig extends AuditableEntity {

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

    /**
     * Quantidade de pontos de vantagem ganhos ao atingir este nível.
     * No sistema legado: 1 ponto por nível.
     */
    @Column(name = "pontos_ganhos", nullable = false)
    @NotNull(message = "Pontos ganhos são obrigatórios")
    @Min(value = 0, message = "Pontos ganhos não podem ser negativos")
    private Integer pontosGanhos = 1;
}
