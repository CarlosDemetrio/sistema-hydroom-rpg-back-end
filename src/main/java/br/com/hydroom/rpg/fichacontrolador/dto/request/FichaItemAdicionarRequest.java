package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request para adicionar um item de catálogo ao inventário de uma ficha.
 */
public record FichaItemAdicionarRequest(
        @NotNull Long itemConfigId,
        @Min(1) int quantidade,
        @Size(max = 500) String notas,
        boolean forcarAdicao
) {}
