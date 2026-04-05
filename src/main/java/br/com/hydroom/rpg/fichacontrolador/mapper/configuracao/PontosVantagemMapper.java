package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreatePontosVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdatePontosVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.PontosVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.model.PontosVantagemConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre PontosVantagemConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PontosVantagemMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    PontosVantagemResponse toResponse(PontosVantagemConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    PontosVantagemConfig toEntity(CreatePontosVantagemRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    void updateEntity(UpdatePontosVantagemRequest request, @MappingTarget PontosVantagemConfig entity);
}
