package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Gênero.
 *
 * @param id ID do gênero
 * @param jogoId ID do jogo
 * @param nome Nome do gênero
 * @param descricao Descrição do gênero
 * @param ordem Ordem de exibição
 * @param ativo Status do gênero
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record GeneroResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordem,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
