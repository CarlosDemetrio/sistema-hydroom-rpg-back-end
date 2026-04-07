package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Sub-recurso de ClassePersonagem que define os equipamentos iniciais
 * disponíveis para um personagem ao escolher uma classe.
 *
 * <p>Equipamentos podem ser obrigatórios ou opcionais (dentro de um grupo de escolha).</p>
 */
@Entity
@Table(name = "classe_equipamentos_iniciais")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class ClasseEquipamentoInicial extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassePersonagem classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_config_id", nullable = false)
    private ItemConfig itemConfig;

    @Column(nullable = false)
    private boolean obrigatorio;

    @Column
    private Integer grupoEscolha;

    @Column(nullable = false)
    @Builder.Default
    private int quantidade = 1;
}
