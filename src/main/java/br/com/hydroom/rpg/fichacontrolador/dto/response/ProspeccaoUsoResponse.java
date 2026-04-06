package br.com.hydroom.rpg.fichacontrolador.dto.response;

import br.com.hydroom.rpg.fichacontrolador.model.enums.ProspeccaoUsoStatus;

import java.time.LocalDateTime;

public record ProspeccaoUsoResponse(
        Long usoId,
        String dadoNome,
        Long dadoProspeccaoConfigId,
        Long fichaId,
        String personagemNome,
        ProspeccaoUsoStatus status,
        LocalDateTime criadoEm
) {}
