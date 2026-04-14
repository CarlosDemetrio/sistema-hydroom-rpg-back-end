package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVantagem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para conversão entre FichaVantagem e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FichaVantagemMapper {

    @Mapping(target = "vantagemConfigId", source = "vantagemConfig.id")
    @Mapping(target = "nomeVantagem", source = "vantagemConfig.nome")
    @Mapping(target = "nivelMaximo", source = "vantagemConfig.nivelMaximo")
    @Mapping(target = "categoriaNome", source = "vantagemConfig.categoriaVantagem.nome")
    @Mapping(target = "tipoVantagem", source = "vantagemConfig.tipoVantagem")
    @Mapping(target = "origem", expression = "java(fichaVantagem.getOrigem() != null ? fichaVantagem.getOrigem().name() : null)")
    FichaVantagemResponse toResponse(FichaVantagem fichaVantagem);
}
