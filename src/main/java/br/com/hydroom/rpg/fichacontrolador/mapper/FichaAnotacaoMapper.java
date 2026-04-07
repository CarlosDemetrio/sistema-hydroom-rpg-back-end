package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAnotacaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.AnotacaoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.FichaAnotacao;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para conversão entre FichaAnotacao e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FichaAnotacaoMapper {

    @Mapping(target = "fichaId", source = "ficha.id")
    @Mapping(target = "autorId", source = "autor.id")
    @Mapping(target = "autorNome", source = "autor.nome")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    @Mapping(target = "pastaPaiId", source = "pastaPai.id")
    AnotacaoResponse toResponse(FichaAnotacao anotacao);

    /**
     * Atualiza campos simples da entidade a partir do request (campos nulos são ignorados).
     *
     * <p>O campo {@code pastaPai} é ignorado aqui e gerenciado manualmente no service,
     * pois o request contém apenas o ID ({@code pastaPaiId}) enquanto a entidade precisa
     * da referência completa ({@code AnotacaoPasta}).</p>
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "pastaPai", ignore = true)
    @Mapping(target = "visivelParaJogador", ignore = true)
    void atualizarEntidade(AtualizarAnotacaoRequest request, @MappingTarget FichaAnotacao anotacao);
}
