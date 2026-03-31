package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaAtributoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.FichaAtributo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para conversão entre FichaAtributo e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FichaAtributoMapper {

    @Mapping(target = "atributoConfigId", source = "atributoConfig.id")
    @Mapping(target = "atributoNome", source = "atributoConfig.nome")
    @Mapping(target = "atributoAbreviacao", source = "atributoConfig.abreviacao")
    FichaAtributoResponse toResponse(FichaAtributo fichaAtributo);
}
