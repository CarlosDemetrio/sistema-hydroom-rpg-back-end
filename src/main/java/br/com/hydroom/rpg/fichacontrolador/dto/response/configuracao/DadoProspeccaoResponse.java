package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Dado de Prospecção.
 *
 * @param id ID do dado de prospecção
 * @param jogoId ID do jogo
 * @param nome Nome do dado
 * @param descricao Descrição do dado
 * @param numeroFaces Número de faces do dado
 * @param ordemExibicao Ordem de exibição
 * @param ativo Status do dado de prospecção
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record DadoProspeccaoResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer numeroFaces,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
