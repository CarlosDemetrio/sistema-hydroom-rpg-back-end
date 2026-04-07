package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Item do inventário de uma ficha de personagem.
 *
 * <p>Pode referenciar um ItemConfig (item de catálogo) ou ser customizado (sem itemConfig).</p>
 * <p>Itens customizados são criados apenas pelo Mestre.</p>
 */
@Entity
@Table(name = "ficha_itens", indexes = {
    @Index(name = "idx_ficha_item_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_item_config", columnList = "item_config_id"),
    @Index(name = "idx_ficha_item_equipado", columnList = "equipado")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class FichaItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_config_id")
    private ItemConfig itemConfig;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    @Builder.Default
    private boolean equipado = false;

    @Column
    private Integer duracaoAtual;

    @Column(nullable = false)
    @Builder.Default
    private int quantidade = 1;

    @Column(precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(length = 500)
    private String notas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raridade_id")
    private RaridadeItemConfig raridade;

    @Column(length = 100)
    private String adicionadoPor;
}
