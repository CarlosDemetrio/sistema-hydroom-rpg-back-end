package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Bônus.
 *
 * @param id ID do bônus
 * @param jogoId ID do jogo
 * @param nome Nome do bônus
 * @param descricao Descrição do bônus
 * @param formulaBase Fórmula para cálculo do bônus
 * @param ordemExibicao Ordem de exibição
 * @param ativo Status do bônus
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record BonusResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    String formulaBase,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
