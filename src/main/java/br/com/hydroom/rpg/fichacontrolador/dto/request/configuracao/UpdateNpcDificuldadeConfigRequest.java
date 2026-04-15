package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.FocoNpc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO para atualizar uma configuração de nível de dificuldade de NPC.
 * Campos null não são alterados.
 *
 * @param nome            Novo nome do nível
 * @param descricao       Nova descrição
 * @param foco            Novo foco de distribuição
 * @param ordemExibicao   Nova ordem de exibição
 * @param valoresAtributo Nova lista completa de valores (substitui a existente quando não-null)
 */
public record UpdateNpcDificuldadeConfigRequest(
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    FocoNpc foco,

    Integer ordemExibicao,

    @Valid
    List<NpcDificuldadeAtributoRequest> valoresAtributo
) {}
