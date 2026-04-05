package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoVantagem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar uma vantagem existente.
 *
 * @param nome Nome da vantagem
 * @param descricao Descrição da vantagem
 * @param nivelMaximo Nível máximo da vantagem
 * @param formulaCusto Fórmula para cálculo do custo
 * @param descricaoEfeito Descrição do efeito da vantagem
 * @param ordemExibicao Ordem de exibição
 */
public record UpdateVantagemRequest(
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    @Size(min = 2, max = 5, message = "Sigla deve ter entre 2 e 5 caracteres")
    String sigla,

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    String descricao,

    @Min(value = 1, message = "Nível máximo deve ser no mínimo 1")
    Integer nivelMaximo,

    @Size(max = 100, message = "Fórmula de custo deve ter no máximo 100 caracteres")
    String formulaCusto,

    @Size(max = 500, message = "Descrição do efeito deve ter no máximo 500 caracteres")
    String descricaoEfeito,

    Integer ordemExibicao,

    Long categoriaVantagemId,

    TipoVantagem tipoVantagem
) {}
