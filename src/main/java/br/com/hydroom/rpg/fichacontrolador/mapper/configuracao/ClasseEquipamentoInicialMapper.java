package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseEquipamentoInicialUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseEquipamentoInicialResponse;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseEquipamentoInicial;
import org.mapstruct.*;

/**
 * Mapper para ClasseEquipamentoInicial.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClasseEquipamentoInicialMapper {

    @Mapping(target = "classeId", source = "classe.id")
    @Mapping(target = "classeNome", source = "classe.nome")
    @Mapping(target = "itemConfigId", source = "itemConfig.id")
    @Mapping(target = "itemConfigNome", source = "itemConfig.nome")
    @Mapping(target = "itemRaridade", source = "itemConfig.raridade.nome")
    @Mapping(target = "itemRaridadeCor", source = "itemConfig.raridade.cor")
    @Mapping(target = "itemCategoria", expression = "java(entity.getItemConfig().getTipo().getCategoria().name())")
    @Mapping(target = "dataCriacao", source = "createdAt")
    ClasseEquipamentoInicialResponse toResponse(ClasseEquipamentoInicial entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void atualizarEntidade(ClasseEquipamentoInicialUpdateRequest request, @MappingTarget ClasseEquipamentoInicial entity);
}
