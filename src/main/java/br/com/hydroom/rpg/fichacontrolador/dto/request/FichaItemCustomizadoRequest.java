package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request para adicionar um item customizado (não-catálogo) ao inventário de uma ficha.
 * Exclusivo do Mestre.
 */
public record FichaItemCustomizadoRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotNull Long raridadeId,
        @NotNull @DecimalMin("0.00") BigDecimal peso,
        @Min(1) int quantidade,
        @Size(max = 500) String notas
) {}
