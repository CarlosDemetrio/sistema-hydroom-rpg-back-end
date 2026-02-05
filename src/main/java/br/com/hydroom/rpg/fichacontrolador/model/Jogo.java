package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa um jogo/campanha de RPG.
 *
 * @author Carlos Demétrio
 */
@Entity
@Table(name = "jogos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jogo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = ValidationMessages.Jogo.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.JOGO_NOME_MIN,
          max = ValidationMessages.Limites.JOGO_NOME_MAX,
          message = ValidationMessages.Jogo.NOME_TAMANHO)
    @Column(nullable = false, length = ValidationMessages.Limites.JOGO_NOME_MAX)
    private String nome;

    @Size(max = ValidationMessages.Limites.JOGO_DESCRICAO_MAX,
          message = ValidationMessages.Jogo.DESCRICAO_TAMANHO)
    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Size(max = ValidationMessages.Limites.JOGO_IMAGEM_URL_MAX,
          message = ValidationMessages.Jogo.IMAGEM_URL_TAMANHO)
    @Column(name = "imagem_url", length = ValidationMessages.Limites.JOGO_IMAGEM_URL_MAX)
    private String imagemUrl;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    /**
     * Participantes do jogo (Mestre + Jogadores).
     */
    @OneToMany(mappedBy = "jogo", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @Builder.Default
    private Set<JogoParticipante> participantes = new HashSet<>();

    /**
     * Adiciona um participante ao jogo.
     */
    public void adicionarParticipante(JogoParticipante participante) {
        participantes.add(participante);
        participante.setJogo(this);
    }

    /**
     * Remove um participante do jogo.
     */
    public void removerParticipante(JogoParticipante participante) {
        participantes.remove(participante);
        participante.setJogo(null);
    }
}
