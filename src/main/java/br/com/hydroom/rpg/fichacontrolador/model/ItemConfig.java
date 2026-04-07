package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuração central de itens do jogo (catálogo de itens).
 *
 * <p>Define as propriedades base de cada item: peso, valor, raridade, tipo,
 * nível mínimo, efeitos que concede e requisitos para uso.</p>
 */
@Entity
@Table(name = "item_configs",
    uniqueConstraints = @UniqueConstraint(name = "uk_item_config_jogo_nome", columnNames = {"jogo_id", "nome"}))
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@ToString(exclude = {"efeitos", "requisitos"})
public class ItemConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raridade_id", nullable = false)
    private RaridadeItemConfig raridade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoItemConfig tipo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal peso;

    @Column
    private Integer valor;

    @Column
    private Integer duracaoPadrao;

    @Column(nullable = false)
    private int nivelMinimo;

    @Column(length = 1000)
    private String propriedades;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false)
    private int ordemExibicao;

    @OneToMany(mappedBy = "itemConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ItemEfeito> efeitos = new ArrayList<>();

    @OneToMany(mappedBy = "itemConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ItemRequisito> requisitos = new ArrayList<>();
}
