package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateMembroCorpoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateMembroCorpoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.MembroCorpoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre MembroCorpoConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MembroCorpoConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    MembroCorpoResponse toResponse(MembroCorpoConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    MembroCorpoConfig toEntity(CreateMembroCorpoRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateMembroCorpoRequest request, @MappingTarget MembroCorpoConfig entity);
}
