package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Índole.
 *
 * @param id ID da índole
 * @param jogoId ID do jogo
 * @param nome Nome da índole
 * @param descricao Descrição da índole
 * @param ordem Ordem de exibição
 * @param ativo Status da índole
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record IndoleResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordem,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
