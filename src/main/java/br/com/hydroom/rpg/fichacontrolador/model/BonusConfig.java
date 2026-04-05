package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuração de bônus calculados do jogo (B.B.A, B.B.D, etc.).
 */
@Entity
@Table(name = "bonus_config", indexes = {
    @Index(name = "idx_bonus_config_jogo", columnList = "jogo_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_bonus_config_jogo_nome", columnNames = {"jogo_id", "nome"}),
    @UniqueConstraint(name = "uk_bonus_config_jogo_sigla", columnNames = {"jogo_id", "sigla"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nome;

    @Size(max = 500)
    @Column(length = 500)
    private String descricao;

    @NotBlank(message = "Sigla é obrigatória")
    @Size(min = 2, max = 5, message = "Sigla deve ter entre 2 e 5 caracteres")
    @Column(name = "sigla", nullable = false, length = 5)
    private String sigla;

    @Size(max = 200)
    @Column(name = "formula_base", length = 200)
    private String formulaBase;

    @Builder.Default
    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;
}
