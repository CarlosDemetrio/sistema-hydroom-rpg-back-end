package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.TipoItemConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.TipoItemConfigUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.TipoItemConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre TipoItemConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TipoItemConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    TipoItemConfigResponse toResponse(TipoItemConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    TipoItemConfig toEntity(TipoItemConfigRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    void updateEntity(TipoItemConfigUpdateRequest request, @MappingTarget TipoItemConfig entity);
}
