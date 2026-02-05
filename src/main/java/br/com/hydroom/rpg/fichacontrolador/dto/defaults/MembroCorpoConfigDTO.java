package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private BigDecimal porcentagemVida; // Porcentagem da vida total (0.01 a 1.00)
    private Integer ordemExibicao;

    /**
     * Cria um MembroCorpoConfigDTO com valores básicos.
     *
     * @param nome Nome do membro (ex: "Cabeça", "Tronco")
     * @param porcentagemVida Porcentagem da vida total (ex: 0.20 = 20%)
     * @param ordemExibicao Ordem de exibição
     */
    public static MembroCorpoConfigDTO of(String nome, BigDecimal porcentagemVida, Integer ordemExibicao) {
        return MembroCorpoConfigDTO.builder()
                .nome(nome)
                .porcentagemVida(porcentagemVida)
                .ordemExibicao(ordemExibicao)
                .build();
    }
}
