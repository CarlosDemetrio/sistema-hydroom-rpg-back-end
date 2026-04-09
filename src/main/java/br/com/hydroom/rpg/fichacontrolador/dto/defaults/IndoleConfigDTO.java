package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Índoles (Alinhamentos).
 * Usado pelo GameDefaultConfigProvider para inicializar índoles de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoleConfigDTO {

    private String nome;
    private String descricao;
    private Integer ordemExibicao;

    public static IndoleConfigDTO of(String nome, String descricao, Integer ordemExibicao) {
        return IndoleConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .build();
    }

    public static IndoleConfigDTO of(String nome, Integer ordemExibicao) {
        return of(nome, null, ordemExibicao);
    }
}
