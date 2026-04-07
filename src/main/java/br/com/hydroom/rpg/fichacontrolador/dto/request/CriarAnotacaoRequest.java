package br.com.hydroom.rpg.fichacontrolador.dto.request;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoAnotacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para criação de uma anotação em uma ficha.
 */
public record CriarAnotacaoRequest(
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    String titulo,

    @NotBlank(message = "Conteúdo é obrigatório")
    String conteudo,

    @NotNull(message = "Tipo de anotação é obrigatório")
    TipoAnotacao tipoAnotacao,

    Boolean visivelParaJogador,

    Long pastaPaiId,
    Boolean visivelParaTodos
) {}
