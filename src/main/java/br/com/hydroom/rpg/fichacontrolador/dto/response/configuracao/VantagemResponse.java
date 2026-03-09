package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Vantagem.
 *
 * @param id ID da vantagem
 * @param jogoId ID do jogo
 * @param nome Nome da vantagem
 * @param descricao Descrição da vantagem
 * @param nivelMaximo Nível máximo da vantagem
 * @param formulaCusto Fórmula para cálculo do custo
 * @param descricaoEfeito Descrição do efeito
 * @param ordemExibicao Ordem de exibição
 * @param ativo Status da vantagem
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record VantagemResponse(
    Long id,
    Long jogoId,
    String nome,
    String sigla,
    String descricao,
    Integer nivelMaximo,
    String formulaCusto,
    String descricaoEfeito,
    Integer ordemExibicao,
    Long categoriaVantagemId,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
