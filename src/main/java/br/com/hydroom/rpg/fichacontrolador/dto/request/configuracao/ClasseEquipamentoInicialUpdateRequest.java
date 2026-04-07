package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request para atualização parcial de equipamento inicial de classe.
 */
public record ClasseEquipamentoInicialUpdateRequest(
        Boolean obrigatorio,
        Integer grupoEscolha,
        @Min(1) @Max(99) Integer quantidade
) {}
