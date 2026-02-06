package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateBonusRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateBonusRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.BonusResponse;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import org.mapstruct.*;

/**
 * Mapper para conversão entre BonusConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BonusConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    BonusResponse toResponse(BonusConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    BonusConfig toEntity(CreateBonusRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateBonusRequest request, @MappingTarget BonusConfig entity);
}
