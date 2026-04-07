package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaItemResponse;
import br.com.hydroom.rpg.fichacontrolador.model.FichaItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para conversão entre FichaItem e seus DTOs.
 *
 * <p>pesoEfetivo é ignorado aqui — calculado no service e injetado no controller.</p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FichaItemMapper {

    @Mapping(target = "fichaId", source = "ficha.id")
    @Mapping(target = "itemConfigId", source = "itemConfig.id")
    @Mapping(target = "raridadeId", source = "raridade.id")
    @Mapping(target = "raridadeNome", source = "raridade.nome")
    @Mapping(target = "raridadeCor", source = "raridade.cor")
    @Mapping(target = "duracaoPadrao", source = "itemConfig.duracaoPadrao")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "pesoEfetivo", ignore = true)
    FichaItemResponse toResponse(FichaItem fichaItem);
}
