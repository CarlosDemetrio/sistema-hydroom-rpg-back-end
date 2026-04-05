package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request para conceder XP a uma ficha (Mestre only).
 */
public record ConcederXpRequest(
        @NotNull(message = "XP é obrigatório")
        @Min(value = 1, message = "XP deve ser pelo menos 1")
        Long xp,

        @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
        String motivo
) {
}
