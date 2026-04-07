package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidade que representa uma pasta organizadora de anotações em uma ficha.
 *
 * <p>Pastas podem ser aninhadas até 3 níveis de profundidade via auto-referência.</p>
 */
@Entity
@Table(
    name = "anotacao_pastas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_anotacao_pasta_ficha_pai_nome",
        columnNames = {"ficha_id", "pasta_pai_id", "nome"}
    ),
    indexes = {
        @Index(name = "idx_anotacao_pasta_ficha", columnList = "ficha_id"),
        @Index(name = "idx_anotacao_pasta_pai",   columnList = "pasta_pai_id")
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnotacaoPasta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasta_pai_id")
    private AnotacaoPasta pastaPai;

    @Column(name = "ordem_exibicao", nullable = false)
    @Builder.Default
    private Integer ordemExibicao = 0;
}
