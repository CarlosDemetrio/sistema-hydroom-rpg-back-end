package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VantagemEfeitoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VantagemPreRequisitoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import org.mapstruct.*;

/**
 * Mapper para conversão entre VantagemConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {VantagemEfeitoMapper.class})
public interface VantagemConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "categoriaVantagemId", source = "categoriaVantagem.id")
    @Mapping(target = "categoriaNome", source = "categoriaVantagem.nome")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    @Mapping(target = "efeitos", source = "efeitos")
    VantagemResponse toResponse(VantagemConfig entity);

    @Mapping(target = "requisitoId", source = "requisito.id")
    @Mapping(target = "requisitoNome", source = "requisito.nome")
    @Mapping(target = "racaId", source = "raca.id")
    @Mapping(target = "racaNome", source = "raca.nome")
    @Mapping(target = "classeId", source = "classe.id")
    @Mapping(target = "classeNome", source = "classe.nome")
    @Mapping(target = "atributoId", source = "atributo.id")
    @Mapping(target = "atributoNome", source = "atributo.nome")
    @Mapping(target = "atributoAbreviacao", source = "atributo.abreviacao")
    @Mapping(target = "aptidaoId", source = "aptidao.id")
    @Mapping(target = "aptidaoNome", source = "aptidao.nome")
    VantagemPreRequisitoResponse toPreRequisitoResponse(VantagemPreRequisito pr);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "categoriaVantagem", ignore = true)
    VantagemConfig toEntity(CreateVantagemRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "categoriaVantagem", ignore = true)
    void updateEntity(UpdateVantagemRequest request, @MappingTarget VantagemConfig entity);
}
