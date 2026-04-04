package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request para conceder XP a uma ficha (Mestre only).
 */
public record ConcederXpRequest(
        @NotNull(message = "XP é obrigatório")
        @Min(value = 0, message = "XP não pode ser negativo")
        Long xp
) {
}
