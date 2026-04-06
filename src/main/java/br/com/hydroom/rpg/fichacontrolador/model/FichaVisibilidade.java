package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Registra o acesso granular de um Jogador específico aos stats de um NPC.
 *
 * <p>Criada pelo Mestre para revelar os stats completos de um NPC para um Jogador.
 * Complementa visivelGlobalmente: um NPC pode ser visível globalmente mas ter
 * stats revelados apenas para jogadores específicos via esta entidade.</p>
 *
 * <p>Par (ficha_id, jogador_id) é único — idempotência garantida pelo banco.</p>
 */
@Entity
@Table(name = "ficha_visibilidades",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_ficha_visibilidade",
        columnNames = {"ficha_id", "jogador_id"}),
    indexes = {
        @Index(name = "idx_ficha_vis_ficha", columnList = "ficha_id"),
        @Index(name = "idx_ficha_vis_jogador", columnList = "jogador_id")
    })
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaVisibilidade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    /**
     * ID do jogador que tem acesso aos stats deste NPC.
     */
    @Column(name = "jogador_id", nullable = false)
    private Long jogadorId;
}
