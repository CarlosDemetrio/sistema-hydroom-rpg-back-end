package br.com.hydroom.rpg.fichacontrolador.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta com dados do usuário autenticado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String email;
    private String nome;
    private String imagemUrl;
    private String provider;
    private Boolean ativo;
}
