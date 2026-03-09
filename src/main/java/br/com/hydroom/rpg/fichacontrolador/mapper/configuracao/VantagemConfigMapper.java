package br.com.hydroom.rpg.fichacontrolador.mapper.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VantagemPreRequisitoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import org.mapstruct.*;

/**
 * Mapper para conversão entre VantagemConfig e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VantagemConfigMapper {

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "categoriaVantagemId", source = "categoriaVantagem.id")
    @Mapping(target = "categoriaNome", source = "categoriaVantagem.nome")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    VantagemResponse toResponse(VantagemConfig entity);

    @Mapping(target = "requisitoId", source = "requisito.id")
    @Mapping(target = "requisitoNome", source = "requisito.nome")
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
