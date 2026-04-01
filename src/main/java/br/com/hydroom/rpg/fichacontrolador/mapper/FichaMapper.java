package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResponse;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import org.mapstruct.*;

/**
 * Mapper para conversão entre Ficha e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FichaMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "racaId", source = "raca.id")
    @Mapping(target = "racaNome", source = "raca.nome")
    @Mapping(target = "classeId", source = "classe.id")
    @Mapping(target = "classeNome", source = "classe.nome")
    @Mapping(target = "generoId", source = "genero.id")
    @Mapping(target = "generoNome", source = "genero.nome")
    @Mapping(target = "indoleId", source = "indole.id")
    @Mapping(target = "indoleNome", source = "indole.nome")
    @Mapping(target = "presencaId", source = "presenca.id")
    @Mapping(target = "presencaNome", source = "presenca.nome")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    FichaResponse toResponse(Ficha ficha);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "raca", ignore = true)
    @Mapping(target = "classe", ignore = true)
    @Mapping(target = "genero", ignore = true)
    @Mapping(target = "indole", ignore = true)
    @Mapping(target = "presenca", ignore = true)
    @Mapping(target = "nivel", constant = "1")
    @Mapping(target = "xp", constant = "0L")
    @Mapping(target = "renascimentos", constant = "0")
    @Mapping(target = "descricao", ignore = true)
    Ficha toEntity(CreateFichaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "jogadorId", ignore = true)
    @Mapping(target = "raca", ignore = true)
    @Mapping(target = "classe", ignore = true)
    @Mapping(target = "genero", ignore = true)
    @Mapping(target = "indole", ignore = true)
    @Mapping(target = "presenca", ignore = true)
    @Mapping(target = "nivel", ignore = true)
    @Mapping(target = "npc", ignore = true)
    @Mapping(target = "descricao", ignore = true)
    void updateEntity(UpdateFichaRequest request, @MappingTarget Ficha ficha);
}
