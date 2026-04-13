package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.HabilidadeConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.HabilidadeConfigUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.HabilidadeConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.model.HabilidadeConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre HabilidadeConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HabilidadeConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    HabilidadeConfigResponse toResponse(HabilidadeConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    HabilidadeConfig toEntity(HabilidadeConfigRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    void updateEntity(HabilidadeConfigUpdateRequest request, @MappingTarget HabilidadeConfig entity);
}
