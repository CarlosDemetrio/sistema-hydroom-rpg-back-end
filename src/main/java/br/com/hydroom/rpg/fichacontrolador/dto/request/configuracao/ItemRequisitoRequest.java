package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoRequisito;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request para criação de ItemRequisito.
 *
 * <p>O campo {@code alvo} é obrigatório para todos os tipos exceto NIVEL
 * (validado no service). {@code valorMinimo} é opcional conforme o tipo.</p>
 */
public record ItemRequisitoRequest(
    @NotNull TipoRequisito tipo,
    @Size(max = 50) String alvo,
    Integer valorMinimo
) {}
