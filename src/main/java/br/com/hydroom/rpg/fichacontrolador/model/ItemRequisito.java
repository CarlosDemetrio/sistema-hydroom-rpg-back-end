package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoRequisito;
import jakarta.persistence.*;
import lombok.*;

/**
 * Requisito para uso/equipe de um item de configuração.
 *
 * <p>Define pré-condições que o personagem deve atender para equipar ou usar o item:
 * nível mínimo, valor de atributo, aptidão, classe, raça, etc.</p>
 *
 * <p>Sem soft delete — gerenciado pelo lifecycle do ItemConfig (orphanRemoval).</p>
 */
@Entity
@Table(name = "item_requisitos")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "itemConfig")
public class ItemRequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_config_id", nullable = false)
    private ItemConfig itemConfig;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoRequisito tipo;

    @Column(length = 50)
    private String alvo;

    @Column
    private Integer valorMinimo;
}
