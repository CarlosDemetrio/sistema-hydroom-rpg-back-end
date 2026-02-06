package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateIndoleRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateIndoleRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.IndoleResponse;
import br.com.hydroom.rpg.fichacontrolador.model.IndoleConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre IndoleConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IndoleConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    IndoleResponse toResponse(IndoleConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    IndoleConfig toEntity(CreateIndoleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateIndoleRequest request, @MappingTarget IndoleConfig entity);
}
