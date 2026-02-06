package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateDadoProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateDadoProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.DadoProspeccaoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre DadoProspeccaoConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DadoProspeccaoConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    DadoProspeccaoResponse toResponse(DadoProspeccaoConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    DadoProspeccaoConfig toEntity(CreateDadoProspeccaoRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateDadoProspeccaoRequest request, @MappingTarget DadoProspeccaoConfig entity);
}
