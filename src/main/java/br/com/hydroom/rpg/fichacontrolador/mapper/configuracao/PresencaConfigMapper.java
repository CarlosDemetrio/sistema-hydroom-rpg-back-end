package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreatePresencaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdatePresencaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.PresencaResponse;
import br.com.hydroom.rpg.fichacontrolador.model.PresencaConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre PresencaConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PresencaConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    PresencaResponse toResponse(PresencaConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    PresencaConfig toEntity(CreatePresencaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdatePresencaRequest request, @MappingTarget PresencaConfig entity);
}
