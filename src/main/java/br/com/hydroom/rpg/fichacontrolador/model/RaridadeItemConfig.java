package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Configuração de raridade de itens do jogo.
 *
 * <p>Define os níveis de raridade (ex: Comum, Incomum, Raro, Épico, Lendário)
 * e quais roles podem adicionar itens dessa raridade ao inventário do personagem.</p>
 */
@Entity
@Table(name = "raridade_item_configs",
    uniqueConstraints = @UniqueConstraint(name = "uk_raridade_item_jogo_nome", columnNames = {"jogo_id", "nome"}))
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class RaridadeItemConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 7)
    private String cor;

    @Column(nullable = false)
    private int ordemExibicao;

    @Column(nullable = false)
    private boolean podeJogadorAdicionar;

    @Column
    private Integer bonusAtributoMin;

    @Column
    private Integer bonusAtributoMax;

    @Column
    private Integer bonusDerivadoMin;

    @Column
    private Integer bonusDerivadoMax;

    @Column(length = 500)
    private String descricao;
}
