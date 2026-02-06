package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Tipo de Aptidão.
 *
 * @param id ID do tipo de aptidão
 * @param jogoId ID do jogo
 * @param nome Nome do tipo de aptidão
 * @param descricao Descrição do tipo de aptidão
 * @param ordemExibicao Ordem de exibição
 * @param ativo Status do tipo de aptidão
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record TipoAptidaoResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
