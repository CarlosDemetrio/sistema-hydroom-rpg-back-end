package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;

/**
 * DTO de default para TipoItemConfig.
 * Utilizado pelo DefaultGameConfigProviderImpl para seed do dataset de tipos de itens.
 */
public record TipoItemConfigDefault(
        String nome,
        CategoriaItem categoria,
        SubcategoriaItem subcategoria,
        boolean requerDuasMaos,
        int ordemExibicao
) {}
