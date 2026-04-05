package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateClasseRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateClasseRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseAptidaoBonusResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseBonusResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseResponse;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseAptidaoBonus;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseBonus;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import org.mapstruct.*;

/**
 * Mapper para conversão entre ClassePersonagem e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClassePersonagemMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    ClasseResponse toResponse(ClassePersonagem entity);

    @Mapping(target = "bonusId", source = "bonus.id")
    @Mapping(target = "bonusNome", source = "bonus.nome")
    ClasseBonusResponse toBonusResponse(ClasseBonus cb);

    @Mapping(target = "aptidaoId", source = "aptidao.id")
    @Mapping(target = "aptidaoNome", source = "aptidao.nome")
    ClasseAptidaoBonusResponse toAptidaoResponse(ClasseAptidaoBonus cab);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    ClassePersonagem toEntity(CreateClasseRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)

    void updateEntity(UpdateClasseRequest request, @MappingTarget ClassePersonagem entity);
}
