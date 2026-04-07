package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarPastaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.AnotacaoPastaResponse;
import br.com.hydroom.rpg.fichacontrolador.model.AnotacaoPasta;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para conversão entre AnotacaoPasta e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnotacaoPastaMapper {

    @Mapping(target = "fichaId", source = "ficha.id")
    @Mapping(target = "pastaPaiId", source = "pastaPai.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    @Mapping(target = "subPastas", ignore = true)
    AnotacaoPastaResponse toResponse(AnotacaoPasta pasta);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ficha", ignore = true)
    @Mapping(target = "pastaPai", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void atualizarEntidade(AtualizarPastaRequest request, @MappingTarget AnotacaoPasta pasta);
}
