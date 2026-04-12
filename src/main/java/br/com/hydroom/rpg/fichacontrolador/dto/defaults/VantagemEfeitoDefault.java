package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito;

import java.math.BigDecimal;

/**
 * DTO default para efeitos de vantagem.
 * Referências a atributo, aptidão, bônus e membro são resolvidas durante a inicialização do jogo.
 */
public record VantagemEfeitoDefault(
        TipoEfeito tipoEfeito,
        String atributoAlvoSigla,
        String aptidaoAlvoNome,
        String bonusAlvoNome,
        String membroAlvoNome,
        BigDecimal valorFixo,
        BigDecimal valorPorNivel,
        String formula,
        String descricaoEfeito
) {}
