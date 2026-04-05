package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.FichaStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidade que representa uma ficha de personagem de RPG.
 * Totalmente normalizada - dados configuráveis armazenados em tabelas relacionadas.
 */
@Entity
@Table(name = "fichas", indexes = {
    @Index(name = "idx_ficha_jogo", columnList = "jogo_id"),
    @Index(name = "idx_ficha_jogador", columnList = "jogador_id"),
    @Index(name = "idx_ficha_nome", columnList = "nome"),
    @Index(name = "idx_ficha_is_npc", columnList = "is_npc")
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ficha extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Jogo é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    /**
     * ID do jogador dono da ficha. Null para NPCs.
     */
    @Column(name = "jogador_id")
    private Long jogadorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id")
    private Raca raca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id")
    private ClassePersonagem classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genero_id")
    private GeneroConfig genero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indole_id")
    private IndoleConfig indole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presenca_id")
    private PresencaConfig presenca;

    @Min(value = 1, message = "Nível mínimo é 1")
    @Builder.Default
    @Column(name = "nivel", nullable = false)
    private Integer nivel = 1;

    @Min(value = 0, message = "XP mínima é 0")
    @Builder.Default
    @Column(name = "xp", nullable = false)
    private Long xp = 0L;

    @Min(value = 0, message = "Renascimentos mínimo é 0")
    @Builder.Default
    @Column(name = "renascimentos", nullable = false)
    private Integer renascimentos = 0;

    @Builder.Default
    @Column(name = "is_npc", nullable = false)
    private boolean isNpc = false;

    /**
     * Descrição textual livre do personagem/NPC.
     * Opcional — especialmente útil para descrever NPCs.
     */
    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    /**
     * Status da ficha.
     * RASCUNHO = criada mas incompleta; COMPLETA = todos os campos obrigatórios preenchidos.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FichaStatus status = FichaStatus.RASCUNHO;
}
