package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para atualização de uma Ficha de personagem.
 * Todos os campos são opcionais (null = não alterar).
 */
public record UpdateFichaRequest(

    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    Long racaId,

    Long classeId,

    Long generoId,

    Long indoleId,

    Long presencaId,

    Long xp,

    Integer renascimentos
) {}
