package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.FocoNpc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO para criar uma nova configuração de nível de dificuldade de NPC.
 *
 * @param jogoId          ID do jogo ao qual a configuração pertence
 * @param nome            Nome do nível (ex: Fácil, Médio, Difícil, Elite, Chefe)
 * @param descricao       Descrição opcional do nível
 * @param foco            Foco de distribuição: FISICO ou MAGICO
 * @param ordemExibicao   Ordem de exibição na interface
 * @param valoresAtributo Lista de valores base por atributo
 */
public record CreateNpcDificuldadeConfigRequest(
    @NotNull(message = "ID do jogo é obrigatório")
    Long jogoId,

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    @NotNull(message = "Foco é obrigatório")
    FocoNpc foco,

    @NotNull(message = "Ordem de exibição é obrigatória")
    Integer ordemExibicao,

    @Valid
    List<NpcDificuldadeAtributoRequest> valoresAtributo
) {}
