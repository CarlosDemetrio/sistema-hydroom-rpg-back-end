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
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "dataFim", ignore = true)
    @Mapping(target = "participantes", ignore = true)
    Jogo toEntity(CriarJogoRequest request);

    /**
     * Converte Jogo para JogoResponse.
     *
     * @param jogo Entidade Jogo
     * @param meuRole Role do usuário logado neste jogo
     * @return JogoResponse com dados completos
     */
    default JogoResponse toResponse(Jogo jogo, RoleJogo meuRole) {
        if (jogo == null) {
            return null;
        }

        return JogoResponse.builder()
            .id(jogo.getId())
            .nome(jogo.getNome())
            .descricao(jogo.getDescricao())
            .dataInicio(jogo.getDataInicio())
            .dataFim(jogo.getDataFim())
            .ativo(jogo.getAtivo())
            .meuRole(meuRole)
            .totalParticipantes((int) jogo.getParticipantes().stream()
                .filter(JogoParticipante::getAtivo)
                .count())
            .criadoEm(jogo.getCriadoEm())
            .atualizadoEm(jogo.getAtualizadoEm())
            .build();
    }

    /**
     * Converte Jogo para JogoResumoResponse.
     *
     * @param jogo Entidade Jogo
     * @param meuRole Role do usuário logado neste jogo
     * @return JogoResumoResponse para listagens
     */
    default JogoResumoResponse toResumoResponse(Jogo jogo, RoleJogo meuRole) {
        if (jogo == null) {
            return null;
        }

        return JogoResumoResponse.builder()
            .id(jogo.getId())
            .nome(jogo.getNome())
            .descricao(jogo.getDescricao())
            .meuRole(meuRole)
            .totalParticipantes((int) jogo.getParticipantes().stream()
                .filter(JogoParticipante::getAtivo)
                .count())
            .ativo(jogo.getAtivo())
            .build();
    }
}
