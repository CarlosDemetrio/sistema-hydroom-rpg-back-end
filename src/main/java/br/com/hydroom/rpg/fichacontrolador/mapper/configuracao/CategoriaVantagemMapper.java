package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateCategoriaVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateCategoriaVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.CategoriaVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.model.CategoriaVantagem;
import org.mapstruct.*;

/**
 * Mapper para conversão entre CategoriaVantagem e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoriaVantagemMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    CategoriaVantagemResponse toResponse(CategoriaVantagem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    CategoriaVantagem toEntity(CreateCategoriaVantagemRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    void updateEntity(UpdateCategoriaVantagemRequest request, @MappingTarget CategoriaVantagem entity);
}
