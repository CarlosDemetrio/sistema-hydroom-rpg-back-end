package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NpcCreateRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    Long racaId,
    Long classeId,
    Long generoId,
    Long indoleId,
    Long presencaId,

    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    String descricao,

    /** Null é interpretado como false no service. */
    Boolean visivelGlobalmente
) {}
