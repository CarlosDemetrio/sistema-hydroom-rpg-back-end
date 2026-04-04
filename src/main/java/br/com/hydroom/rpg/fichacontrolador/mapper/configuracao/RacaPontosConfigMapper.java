package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RacaPontosConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaPontosConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.model.RacaPontosConfig;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RacaPontosConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "raca", ignore = true)
    RacaPontosConfig toEntity(RacaPontosConfigRequest request);

    @Mapping(target = "racaId", source = "raca.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    RacaPontosConfigResponse toResponse(RacaPontosConfig entity);

    List<RacaPontosConfigResponse> toResponseList(List<RacaPontosConfig> entities);
}
