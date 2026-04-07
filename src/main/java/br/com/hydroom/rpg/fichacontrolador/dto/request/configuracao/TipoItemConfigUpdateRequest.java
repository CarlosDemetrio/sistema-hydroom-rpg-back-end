package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import jakarta.validation.constraints.*;

/**
 * Request DTO para atualização parcial de tipo de item.
 * Todos os campos são opcionais — apenas os não-nulos serão atualizados.
 */
public record TipoItemConfigUpdateRequest(
    @Size(max = 100) String nome,
    CategoriaItem categoria,
    SubcategoriaItem subcategoria,
    Boolean requerDuasMaos,
    @Min(1) Integer ordemExibicao,
    @Size(max = 300) String descricao
) {}
