package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VantagemEfeitoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemEfeito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para VantagemEfeito.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VantagemEfeitoMapper {

    @Mapping(target = "vantagemConfigId", source = "vantagemConfig.id")
    @Mapping(target = "atributoAlvoId", source = "atributoAlvo.id")
    @Mapping(target = "atributoAlvoNome", source = "atributoAlvo.nome")
    @Mapping(target = "aptidaoAlvoId", source = "aptidaoAlvo.id")
    @Mapping(target = "aptidaoAlvoNome", source = "aptidaoAlvo.nome")
    @Mapping(target = "bonusAlvoId", source = "bonusAlvo.id")
    @Mapping(target = "bonusAlvoNome", source = "bonusAlvo.nome")
    @Mapping(target = "membroAlvoId", source = "membroAlvo.id")
    @Mapping(target = "membroAlvoNome", source = "membroAlvo.nome")
    @Mapping(target = "dataCriacao", source = "createdAt")
    VantagemEfeitoResponse toResponse(VantagemEfeito entity);
}
