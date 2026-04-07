package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Configuração de tipo de item do jogo.
 *
 * <p>Define as categorias e subcategorias de itens disponíveis
 * (ex: Arma/Espada, Armadura/Leve, Acessório/Anel).</p>
 */
@Entity
@Table(name = "tipo_item_configs",
    uniqueConstraints = @UniqueConstraint(name = "uk_tipo_item_jogo_nome", columnNames = {"jogo_id", "nome"}))
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class TipoItemConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CategoriaItem categoria;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private SubcategoriaItem subcategoria;

    @Column(nullable = false)
    private boolean requerDuasMaos;

    @Column(nullable = false)
    private int ordemExibicao;

    @Column(length = 300)
    private String descricao;
}
