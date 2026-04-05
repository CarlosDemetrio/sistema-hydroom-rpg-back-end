package br.com.hydroom.rpg.fichacontrolador.dto.response;

import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;

import java.time.LocalDateTime;

/**
 * Response DTO para JogoParticipante.
 */
public record ParticipanteResponse(
    Long id,
    Long jogoId,
    Long usuarioId,
    String nomeUsuario,
    RoleJogo role,
    StatusParticipante status,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
