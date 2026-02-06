package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.AptidaoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre AptidaoConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AptidaoConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "tipoAptidaoId", source = "tipoAptidao.id")
    @Mapping(target = "tipoAptidaoNome", source = "tipoAptidao.nome")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    AptidaoResponse toResponse(AptidaoConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "tipoAptidao", ignore = true)

    AptidaoConfig toEntity(CreateAptidaoRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "tipoAptidao", ignore = true)

    void updateEntity(UpdateAptidaoRequest request, @MappingTarget AptidaoConfig entity);
}
