package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO para Membro do Corpo.
 *
 * @param id ID do membro do corpo
 * @param jogoId ID do jogo
 * @param nome Nome do membro do corpo
 * @param porcentagemVida Porcentagem da vida total
 * @param ordemExibicao Ordem de exibição
 * @param ativo Status do membro do corpo
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record MembroCorpoResponse(
    Long id,
    Long jogoId,
    String nome,
    BigDecimal porcentagemVida,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
