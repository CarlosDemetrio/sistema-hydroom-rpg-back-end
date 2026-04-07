package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RaridadeItemConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RaridadeItemConfigUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RaridadeItemConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre RaridadeItemConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RaridadeItemConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    RaridadeItemConfigResponse toResponse(RaridadeItemConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    RaridadeItemConfig toEntity(RaridadeItemConfigRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    void updateEntity(RaridadeItemConfigUpdateRequest request, @MappingTarget RaridadeItemConfig entity);
}
