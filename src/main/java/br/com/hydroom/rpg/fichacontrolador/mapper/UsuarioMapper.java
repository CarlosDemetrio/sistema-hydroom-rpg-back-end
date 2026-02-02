package br.com.hydroom.rpg.fichacontrolador.mapper;

import br.com.hydroom.rpg.fichacontrolador.dto.response.UsuarioResponse;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para conversão entre Usuario e DTOs.
 * MapStruct gera a implementação automaticamente.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {

    @Mapping(target = "fotoPerfil", source = "imagemUrl")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    UsuarioResponse toResponse(Usuario usuario);
}
