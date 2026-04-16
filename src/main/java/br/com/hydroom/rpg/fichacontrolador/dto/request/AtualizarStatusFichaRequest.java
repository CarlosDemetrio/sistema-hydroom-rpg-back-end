package br.com.hydroom.rpg.fichacontrolador.dto.request;

import br.com.hydroom.rpg.fichacontrolador.model.enums.FichaStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request para alteração de status de uma ficha pelo Mestre.
 * Apenas ATIVA, MORTA e ABANDONADA são aceitos — RASCUNHO e COMPLETA são gerenciados internamente.
 */
public record AtualizarStatusFichaRequest(
        @NotNull(message = "status é obrigatório")
        FichaStatus status
) {}
