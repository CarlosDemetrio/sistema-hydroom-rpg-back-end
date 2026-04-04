package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClassePontosConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClassePontosConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePontosConfig;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClassePontosConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classePersonagem", ignore = true)
    ClassePontosConfig toEntity(ClassePontosConfigRequest request);

    @Mapping(target = "classePersonagemId", source = "classePersonagem.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    ClassePontosConfigResponse toResponse(ClassePontosConfig entity);

    List<ClassePontosConfigResponse> toResponseList(List<ClassePontosConfig> entities);
}
