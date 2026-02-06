package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateGeneroRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateGeneroRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.GeneroResponse;
import br.com.hydroom.rpg.fichacontrolador.model.GeneroConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre GeneroConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GeneroConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    GeneroResponse toResponse(GeneroConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    GeneroConfig toEntity(CreateGeneroRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateGeneroRequest request, @MappingTarget GeneroConfig entity);
}
