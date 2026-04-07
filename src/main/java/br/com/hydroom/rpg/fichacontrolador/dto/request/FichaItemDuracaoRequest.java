package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request para decrementar ou restaurar a durabilidade de um item da ficha.
 */
public record FichaItemDuracaoRequest(
        @NotNull @Min(1) Integer decremento,
        boolean restaurar
) {}
