package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseVantagemPreDefinidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseVantagemPreDefinidaResponse;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseVantagemPreDefinida;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClasseVantagemPreDefinidaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classePersonagem", ignore = true)
    @Mapping(target = "vantagemConfig", ignore = true)
    ClasseVantagemPreDefinida toEntity(ClasseVantagemPreDefinidaRequest request);

    @Mapping(target = "classePersonagemId", source = "classePersonagem.id")
    @Mapping(target = "vantagemConfigId", source = "vantagemConfig.id")
    @Mapping(target = "vantagemConfigNome", source = "vantagemConfig.nome")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    ClasseVantagemPreDefinidaResponse toResponse(ClasseVantagemPreDefinida entity);

    List<ClasseVantagemPreDefinidaResponse> toResponseList(List<ClasseVantagemPreDefinida> entities);
}
