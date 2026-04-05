package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

public record ClasseVantagemPreDefinidaResponse(
    Long id,
    Long classePersonagemId,
    Integer nivel,
    Long vantagemConfigId,
    String vantagemConfigNome,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
