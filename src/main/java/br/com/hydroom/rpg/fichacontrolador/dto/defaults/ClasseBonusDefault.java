package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import java.math.BigDecimal;

/**
 * DTO default para bônus de derivados concedidos por classe.
 * A referência ao bônus é feita por nome e resolvida durante a inicialização.
 */
public record ClasseBonusDefault(
        String bonusNome,
        BigDecimal valorPorNivel,
        Integer ordemExibicao
) {}
