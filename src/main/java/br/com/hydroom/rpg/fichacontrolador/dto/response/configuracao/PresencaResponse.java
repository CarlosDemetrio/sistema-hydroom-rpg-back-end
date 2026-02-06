package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Presença.
 *
 * @param id ID da presença
 * @param jogoId ID do jogo
 * @param nome Nome da presença
 * @param descricao Descrição da presença
 * @param ordem Ordem de exibição
 * @param ativo Status da presença
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record PresencaResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordem,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
