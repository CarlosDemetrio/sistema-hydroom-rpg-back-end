package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Item para reordenação em batch de configurações.
 */
public record ReordenarItemRequest(
        @NotNull Long id,
        @Min(0) int ordemExibicao
) {}
