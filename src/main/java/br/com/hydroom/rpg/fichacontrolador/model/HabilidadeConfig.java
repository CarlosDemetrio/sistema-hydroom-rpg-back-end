package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Configuração de habilidade do jogo.
 *
 * <p>Habilidades são ações especiais que personagens podem executar em combate ou fora dele.
 * A diferença desta entidade em relação às demais configurações é que JOGADOR
 * também pode criar, editar e deletar — não apenas MESTRE.</p>
 */
@Entity
@Table(
    name = "habilidade_config",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_habilidade_config_jogo_nome",
        columnNames = {"jogo_id", "nome"}
    )
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class HabilidadeConfig extends BaseEntity implements ConfiguracaoEntity {

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

    @Size(max = 500)
    @Column(name = "dano_efeito", length = 500)
    private String danoEfeito;

    @NotNull
    @Min(0)
    @Builder.Default
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;
}
