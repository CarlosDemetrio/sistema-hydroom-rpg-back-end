package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Raça.
 *
 * @param id ID da raça
 * @param jogoId ID do jogo
 * @param nome Nome da raça
 * @param descricao Descrição da raça
 * @param ordemExibicao Ordem de exibição
 * @param ativo Status da raça
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record RacaResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
