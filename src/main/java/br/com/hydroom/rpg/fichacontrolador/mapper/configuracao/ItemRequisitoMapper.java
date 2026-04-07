package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ItemRequisitoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.ItemRequisito;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper para ItemRequisito.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemRequisitoMapper {

    ItemRequisitoResponse toResponse(ItemRequisito entity);
}
