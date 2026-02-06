package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateTipoAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateTipoAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.TipoAptidaoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import org.mapstruct.*;

/**
 * Mapper para conversão entre TipoAptidao e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TipoAptidaoMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    TipoAptidaoResponse toResponse(TipoAptidao entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    TipoAptidao toEntity(CreateTipoAptidaoRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateTipoAptidaoRequest request, @MappingTarget TipoAptidao entity);
}
