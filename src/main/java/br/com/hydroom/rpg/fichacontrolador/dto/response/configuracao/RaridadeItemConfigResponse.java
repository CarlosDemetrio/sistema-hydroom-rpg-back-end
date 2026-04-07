package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para RaridadeItemConfig.
 */
public record RaridadeItemConfigResponse(
    Long id,
    Long jogoId,
    String nome,
    String cor,
    int ordemExibicao,
    boolean podeJogadorAdicionar,
    Integer bonusAtributoMin,
    Integer bonusAtributoMax,
    Integer bonusDerivadoMin,
    Integer bonusDerivadoMax,
    String descricao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
