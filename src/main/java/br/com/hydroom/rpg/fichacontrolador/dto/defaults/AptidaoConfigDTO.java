package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Aptidões.
 * Usado pelo GameDefaultConfigProvider para inicializar aptidões de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptidaoConfigDTO {

    private String nome;
    private String tipo;               // "FISICA" ou "MENTAL"
    private String descricao;
    private Integer ordemExibicao;

    /**
     * Cria um AptidaoConfigDTO com valores básicos.
     */
    public static AptidaoConfigDTO of(String nome, String tipo, String descricao, Integer ordemExibicao) {
        return AptidaoConfigDTO.builder()
                .nome(nome)
                .tipo(tipo)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .build();
    }
}
