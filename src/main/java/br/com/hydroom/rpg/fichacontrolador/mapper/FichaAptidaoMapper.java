package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaAptidaoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.FichaAptidao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para conversão entre FichaAptidao e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FichaAptidaoMapper {

    @Mapping(target = "aptidaoConfigId", source = "aptidaoConfig.id")
    @Mapping(target = "aptidaoNome", source = "aptidaoConfig.nome")
    FichaAptidaoResponse toResponse(FichaAptidao fichaAptidao);
}
