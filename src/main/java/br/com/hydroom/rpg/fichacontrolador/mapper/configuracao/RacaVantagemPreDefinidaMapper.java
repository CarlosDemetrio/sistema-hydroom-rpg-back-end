package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RacaVantagemPreDefinidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaVantagemPreDefinidaResponse;
import br.com.hydroom.rpg.fichacontrolador.model.RacaVantagemPreDefinida;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RacaVantagemPreDefinidaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "raca", ignore = true)
    @Mapping(target = "vantagemConfig", ignore = true)
    RacaVantagemPreDefinida toEntity(RacaVantagemPreDefinidaRequest request);

    @Mapping(target = "racaId", source = "raca.id")
    @Mapping(target = "vantagemConfigId", source = "vantagemConfig.id")
    @Mapping(target = "vantagemConfigNome", source = "vantagemConfig.nome")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    RacaVantagemPreDefinidaResponse toResponse(RacaVantagemPreDefinida entity);

    List<RacaVantagemPreDefinidaResponse> toResponseList(List<RacaVantagemPreDefinida> entities);
}
