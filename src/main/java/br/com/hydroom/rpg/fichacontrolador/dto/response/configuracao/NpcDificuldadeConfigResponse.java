package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.FocoNpc;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO para NpcDificuldadeConfig.
 *
 * @param id                   ID da configuração
 * @param jogoId               ID do jogo
 * @param nome                 Nome do nível de dificuldade
 * @param descricao            Descrição opcional
 * @param foco                 Foco de distribuição (FISICO ou MAGICO)
 * @param ordemExibicao        Ordem de exibição
 * @param valoresAtributo      Lista de valores base por atributo
 * @param dataCriacao          Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record NpcDificuldadeConfigResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    FocoNpc foco,
    Integer ordemExibicao,
    List<NpcDificuldadeAtributoResponse> valoresAtributo,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
