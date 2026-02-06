package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateNivelRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateNivelRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.NivelResponse;
import br.com.hydroom.rpg.fichacontrolador.model.NivelConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre NivelConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NivelConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    NivelResponse toResponse(NivelConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    NivelConfig toEntity(CreateNivelRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "nivel", ignore = true)

    void updateEntity(UpdateNivelRequest request, @MappingTarget NivelConfig entity);
}
