package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Valor base de um atributo dentro de um NpcDificuldadeConfig.
 *
 * <p>Cada registro associa um AtributoConfig a um NpcDificuldadeConfig com um valorBase pré-definido.
 * Ao criar um NPC com este nível de dificuldade, o atributo correspondente é preenchido com este valor.</p>
 */
@Entity
@Table(name = "npc_dificuldade_atributo", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_npc_dificuldade_atributo",
        columnNames = {"npc_dificuldade_config_id", "atributo_config_id"}
    )
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = "npcDificuldadeConfig")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NpcDificuldadeAtributo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_dificuldade_config_id", nullable = false)
    private NpcDificuldadeConfig npcDificuldadeConfig;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_config_id", nullable = false)
    private AtributoConfig atributoConfig;

    @NotNull
    @Column(name = "valor_base", nullable = false)
    private Integer valorBase;
}
