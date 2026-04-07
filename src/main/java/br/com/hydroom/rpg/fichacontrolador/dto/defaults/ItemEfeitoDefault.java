package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;

/**
 * DTO de default para ItemEfeito.
 *
 * <ul>
 *   <li>{@code bonusAlvoNome}    — nome exato do BonusConfig (ex: "B.B.A", "Defesa"). Nulo se não aplicável.</li>
 *   <li>{@code atributoAlvoNome} — abreviação do AtributoConfig (ex: "FOR"). Nulo se não aplicável.</li>
 *   <li>{@code valorFixo}        — valor fixo do bônus (ex: +1, +2).</li>
 * </ul>
 */
public record ItemEfeitoDefault(
        TipoItemEfeito tipoEfeito,
        String bonusAlvoNome,
        String atributoAlvoNome,
        Integer valorFixo
) {}
