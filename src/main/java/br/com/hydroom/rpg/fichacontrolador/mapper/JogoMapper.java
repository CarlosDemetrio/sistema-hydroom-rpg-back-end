package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.JogoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.JogoResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para conversão entre Jogo e seus DTOs.
 *
 * @author Carlos Demétrio
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface JogoMapper {

    /**
     * Converte CriarJogoRequest para Jogo.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogoAtivo", constant = "false")
    @Mapping(target = "dataFim", ignore = true)
    @Mapping(target = "participantes", ignore = true)
    @Mapping(target = "imagemUrl", ignore = true)
    Jogo toEntity(CriarJogoRequest request);

    /**
     * Converte Jogo para JogoResponse.
     *
     * @param jogo Entidade Jogo
     * @return JogoResponse com dados completos
     */
    @Mapping(target = "ativo", expression = "java(jogo.isActive())")
    @Mapping(target = "meuRole", ignore = true)
    @Mapping(target = "totalParticipantes", expression = "java((int) (jogo.getParticipantes() == null ? 0 : jogo.getParticipantes().stream().filter(p -> p.isActive()).count()))")
    JogoResponse toResponse(Jogo jogo);

    /**
     * Converte Jogo para JogoResumoResponse.
     *
     * @param jogo Entidade Jogo
     * @return JogoResumoResponse para listagens
     */
    @Mapping(target = "ativo", expression = "java(jogo.isActive())")
    @Mapping(target = "meuRole", ignore = true)
    @Mapping(target = "totalParticipantes", expression = "java((int) (jogo.getParticipantes() == null ? 0 : jogo.getParticipantes().stream().filter(p -> p.isActive()).count()))")
    JogoResumoResponse toResumoResponse(Jogo jogo);
}
