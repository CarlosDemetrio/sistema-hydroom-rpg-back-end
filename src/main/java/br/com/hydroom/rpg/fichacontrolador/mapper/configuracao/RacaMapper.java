package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateRacaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateRacaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaResponse;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import org.mapstruct.*;

/**
 * Mapper para conversão entre Raca e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RacaMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    RacaResponse toResponse(Raca entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    Raca toEntity(CreateRacaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateRacaRequest request, @MappingTarget Raca entity);
}
