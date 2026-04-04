package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

public record ClassePontosConfigResponse(
    Long id,
    Long classePersonagemId,
    Integer nivel,
    Integer pontosAtributo,
    Integer pontosVantagem,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
