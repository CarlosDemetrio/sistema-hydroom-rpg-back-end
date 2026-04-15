package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateNpcDificuldadeConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateNpcDificuldadeConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.NpcDificuldadeAtributoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.NpcDificuldadeConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeAtributo;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre NpcDificuldadeConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NpcDificuldadeConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    NpcDificuldadeConfigResponse toResponse(NpcDificuldadeConfig entity);

    @Mapping(target = "atributoId", source = "atributoConfig.id")
    @Mapping(target = "atributoNome", source = "atributoConfig.nome")
    NpcDificuldadeAtributoResponse toAtributoResponse(NpcDificuldadeAtributo atributo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "valoresAtributo", ignore = true)
    NpcDificuldadeConfig toEntity(CreateNpcDificuldadeConfigRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "valoresAtributo", ignore = true)
    void updateEntity(UpdateNpcDificuldadeConfigRequest request, @MappingTarget NpcDificuldadeConfig entity);
}
