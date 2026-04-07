package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import jakarta.validation.constraints.*;

/**
 * Request DTO para criar um novo tipo de item.
 */
public record TipoItemConfigRequest(
    @NotNull Long jogoId,
    @NotBlank @Size(max = 100) String nome,
    @NotNull CategoriaItem categoria,
    SubcategoriaItem subcategoria,
    boolean requerDuasMaos,
    @NotNull @Min(1) Integer ordemExibicao,
    @Size(max = 300) String descricao
) {}
