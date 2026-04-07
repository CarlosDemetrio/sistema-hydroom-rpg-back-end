package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request para atualização parcial de uma pasta de anotações.
 */
public record AtualizarPastaRequest(
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    Integer ordemExibicao
) {}
