package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Membros do Corpo.
 * Usado pelo GameDefaultConfigProvider para inicializar membros do corpo de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroCorpoConfigDTO {

    private String nome;
    private Integer ordemExibicao;

    /**
     * Cria um MembroCorpoConfigDTO com valores básicos.
     */
    public static MembroCorpoConfigDTO of(String nome, Integer ordemExibicao) {
        return MembroCorpoConfigDTO.builder()
                .nome(nome)
                .ordemExibicao(ordemExibicao)
                .build();
    }
}
