package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;
import jakarta.persistence.*;
import lombok.*;

/**
 * Efeito concreto de um item de configuração.
 *
 * <p>Define o que o item faz ao ser equipado pelo personagem: bônus a atributo,
 * aptidão, derivado, vida, essência, fórmula customizada ou modificação de dado.</p>
 *
 * <p>Sem soft delete — gerenciado pelo lifecycle do ItemConfig (orphanRemoval).</p>
 */
@Entity
@Table(name = "item_efeitos")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "itemConfig")
public class ItemEfeito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_config_id", nullable = false)
    private ItemConfig itemConfig;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoItemEfeito tipoEfeito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_alvo_id")
    private AtributoConfig atributoAlvo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aptidao_alvo_id")
    private AptidaoConfig aptidaoAlvo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_alvo_id")
    private BonusConfig bonusAlvo;

    @Column
    private Integer valorFixo;

    @Column(length = 200)
    private String formula;

    @Column(length = 300)
    private String descricaoEfeito;
}
