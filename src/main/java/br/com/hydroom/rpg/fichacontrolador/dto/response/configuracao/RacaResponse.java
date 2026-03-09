package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO para Raça.
 */
public record RacaResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordemExibicao,
    List<RacaBonusAtributoResponse> bonusAtributos,
    List<RacaClassePermitidaResponse> classesPermitidas,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
