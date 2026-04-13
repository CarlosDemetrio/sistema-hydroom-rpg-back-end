package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para HabilidadeConfig.
 */
public record HabilidadeConfigResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    String danoEfeito,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
