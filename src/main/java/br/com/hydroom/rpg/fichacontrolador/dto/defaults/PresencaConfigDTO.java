package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Níveis de Presença.
 * Usado pelo GameDefaultConfigProvider para inicializar presenças de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresencaConfigDTO {

    private String nome;
    private String descricao;
    private Integer ordemExibicao;

    public static PresencaConfigDTO of(String nome, String descricao, Integer ordemExibicao) {
        return PresencaConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .build();
    }

    public static PresencaConfigDTO of(String nome, Integer ordemExibicao) {
        return of(nome, null, ordemExibicao);
    }
}
