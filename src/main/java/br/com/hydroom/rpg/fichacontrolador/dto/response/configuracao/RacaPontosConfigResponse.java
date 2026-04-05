package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

public record RacaPontosConfigResponse(
    Long id,
    Long racaId,
    Integer nivel,
    Integer pontosAtributo,
    Integer pontosVantagem,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
