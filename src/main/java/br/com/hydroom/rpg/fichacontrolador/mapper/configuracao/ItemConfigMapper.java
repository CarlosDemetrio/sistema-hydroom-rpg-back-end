package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemConfigUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ItemConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ItemConfigResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import org.mapstruct.*;

/**
 * Mapper principal para ItemConfig, incluindo sub-entidades via uses.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {ItemEfeitoMapper.class, ItemRequisitoMapper.class}
)
public interface ItemConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "raridadeId", source = "raridade.id")
    @Mapping(target = "raridadeNome", source = "raridade.nome")
    @Mapping(target = "raridadeCor", source = "raridade.cor")
    @Mapping(target = "tipoId", source = "tipo.id")
    @Mapping(target = "tipoNome", source = "tipo.nome")
    @Mapping(target = "categoria", source = "tipo.categoria")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    ItemConfigResponse toResponse(ItemConfig entity);

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "raridadeId", source = "raridade.id")
    @Mapping(target = "raridadeNome", source = "raridade.nome")
    @Mapping(target = "raridadeCor", source = "raridade.cor")
    @Mapping(target = "tipoId", source = "tipo.id")
    @Mapping(target = "tipoNome", source = "tipo.nome")
    @Mapping(target = "categoria", source = "tipo.categoria")
    ItemConfigResumoResponse toResumoResponse(ItemConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "raridade", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "efeitos", ignore = true)
    @Mapping(target = "requisitos", ignore = true)
    ItemConfig toEntity(ItemConfigRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "raridade", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "efeitos", ignore = true)
    @Mapping(target = "requisitos", ignore = true)
    void updateEntity(ItemConfigUpdateRequest request, @MappingTarget ItemConfig entity);
}
