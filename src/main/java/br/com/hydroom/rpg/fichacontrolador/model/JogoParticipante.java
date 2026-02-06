package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.audit.AuditableEntity;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Representa a participação de um usuário em um jogo.
 *
 * @author Carlos Demétrio
 */
@Entity
@Table(
    name = "jogo_participantes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_jogo_usuario",
        columnNames = {"jogo_id", "usuario_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JogoParticipante extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Jogo é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "Role é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoleJogo role = RoleJogo.JOGADOR;

    /**
     * Verifica se o participante é o mestre do jogo.
     */
    public boolean isMestre() {
        return RoleJogo.MESTRE.equals(role);
    }

    /**
     * Verifica se o participante é um jogador.
     */
    public boolean isJogador() {
        return RoleJogo.JOGADOR.equals(role);
    }
}
