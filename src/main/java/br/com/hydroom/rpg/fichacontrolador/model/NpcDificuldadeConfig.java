package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração de nível de dificuldade para NPCs.
 *
 * <p>O Mestre define níveis como Fácil, Médio, Difícil, Elite, Chefe com valores de atributo
 * pré-definidos. Ao criar um NPC, o Mestre seleciona o nível e os atributos já vêm preenchidos.</p>
 */
@Entity
@Table(name = "npc_dificuldade_config", uniqueConstraints = {
    @UniqueConstraint(name = "uk_npc_dificuldade_config_jogo_nome", columnNames = {"jogo_id", "nome"})
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = "valoresAtributo")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NpcDificuldadeConfig extends BaseEntity implements ConfiguracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 500)
    @Column(length = 500)
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "foco", nullable = false, length = 10)
    private FocoNpc foco;

    @Builder.Default
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;

    @OneToMany(mappedBy = "npcDificuldadeConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NpcDificuldadeAtributo> valoresAtributo = new ArrayList<>();
}
