package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

public record RacaVantagemPreDefinidaResponse(
    Long id,
    Long racaId,
    Integer nivel,
    Long vantagemConfigId,
    String vantagemConfigNome,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
