package br.com.hydroom.rpg.fichacontrolador.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String fotoPerfil; // Alias para imagemUrl (compatibilidade frontend)
    private String provider;
    private Boolean ativo;
    private String role; // MESTRE ou JOGADOR
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;

    // Jogo ativo do mestre (null se jogador ou se não houver jogo ativo)
    private JogoResumoResponse jogoAtivo;
}
