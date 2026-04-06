package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.ProspeccaoUsoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Registra o uso de um dado de prospecção por um Jogador ou Mestre.
 *
 * <p>O fluxo semântico é:</p>
 * <ol>
 *   <li>Jogador usa o dado → status PENDENTE, quantidade decrementada</li>
 *   <li>Mestre confirma → status CONFIRMADO (quantidade não muda)</li>
 *   <li>Mestre reverte → status REVERTIDO, quantidade restaurada</li>
 * </ol>
 *
 * <p>ProspeccaoUso extende BaseEntity mas o histórico não é apagado por soft delete —
 * o campo deleted_at permanece sempre nulo. Apenas REVERTIDO/CONFIRMADO registram o desfecho.</p>
 */
@Entity
@Table(name = "prospeccao_usos",
    indexes = {
        @Index(name = "idx_pros_uso_ficha_pros", columnList = "ficha_prospeccao_id"),
        @Index(name = "idx_pros_uso_status", columnList = "status")
    })
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProspeccaoUso extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_prospeccao_id", nullable = false)
    private FichaProspeccao fichaProspeccao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProspeccaoUsoStatus status = ProspeccaoUsoStatus.PENDENTE;
}
