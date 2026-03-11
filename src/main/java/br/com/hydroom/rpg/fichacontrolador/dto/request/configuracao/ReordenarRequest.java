package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request para reordenação em batch de configurações.
 */
public record ReordenarRequest(
        @NotEmpty @Valid List<ReordenarItemRequest> itens
) {}
