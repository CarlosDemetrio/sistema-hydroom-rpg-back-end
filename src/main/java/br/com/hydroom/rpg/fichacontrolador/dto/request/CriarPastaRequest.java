package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para criação de uma pasta de anotações.
 */
public record CriarPastaRequest(
    @NotBlank(message = "Nome da pasta é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    Long pastaPaiId,

    Integer ordemExibicao
) {}
