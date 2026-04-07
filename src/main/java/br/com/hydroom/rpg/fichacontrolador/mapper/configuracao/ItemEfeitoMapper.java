package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ItemEfeitoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.ItemEfeito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para ItemEfeito.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemEfeitoMapper {

    @Mapping(target = "atributoAlvoId", source = "atributoAlvo.id")
    @Mapping(target = "aptidaoAlvoId", source = "aptidaoAlvo.id")
    @Mapping(target = "bonusAlvoId", source = "bonusAlvo.id")
    ItemEfeitoResponse toResponse(ItemEfeito entity);
}
