package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarImagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaImagemResponse;
import br.com.hydroom.rpg.fichacontrolador.model.FichaImagem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para conversão entre FichaImagem e seus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FichaImagemMapper {

    @Mapping(target = "fichaId", source = "ficha.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    FichaImagemResponse toResponse(FichaImagem imagem);

    /**
     * Atualiza apenas título e ordemExibicao — campos nulos são ignorados.
     * urlCloudinary, publicId e tipoImagem são imutáveis e ignorados aqui.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void atualizarEntidade(AtualizarImagemRequest request, @MappingTarget FichaImagem imagem);
}
