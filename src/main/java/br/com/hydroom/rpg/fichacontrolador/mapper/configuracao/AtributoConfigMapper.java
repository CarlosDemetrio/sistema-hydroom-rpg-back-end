package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.AtributoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre AtributoConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AtributoConfigMapper {

    /**
     * Converte AtributoConfig para AtributoResponse.
     */
    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    AtributoResponse toResponse(AtributoConfig entity);

    /**
     * Converte CreateAtributoRequest para AtributoConfig.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true) // Será setado no service

    AtributoConfig toEntity(CreateAtributoRequest request);

    /**
     * Atualiza AtributoConfig com dados de UpdateAtributoRequest.
     * Campos null não serão atualizados.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateAtributoRequest request, @MappingTarget AtributoConfig entity);
}
