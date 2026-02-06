package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuração de vantagem do jogo.
 * Vantagens são compradas com pontos e não podem ser revertidas.
 */
@Entity
@Table(name = "vantagem_config", indexes = {
    @Index(name = "idx_vantagem_config_jogo", columnList = "jogo_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_vantagem_config_jogo_nome", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VantagemConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descricao;

    @NotNull
    @Min(1)
    @Column(name = "nivel_maximo", nullable = false)
    @Builder.Default
    private Integer nivelMaximo = 10;

    @NotBlank
    @Size(max = 100)
    @Column(name = "formula_custo", nullable = false, length = 100)
    private String formulaCusto; // Ex: "NIVEL * 2", "NIVEL * NIVEL", etc.

    @Size(max = 500)
    @Column(name = "descricao_efeito", length = 500)
    private String descricaoEfeito;

    @Builder.Default
    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;

    /**
     * Calcula o custo para subir do nível atual para o próximo nível.
     * A fórmula é configurável pelo Mestre.
     */
    public int calcularCusto(int nivelAtual) {
        // Implementação será feita no service com parser de expressões
        // Por enquanto, apenas um placeholder
        return nivelAtual * 2;
    }
}
