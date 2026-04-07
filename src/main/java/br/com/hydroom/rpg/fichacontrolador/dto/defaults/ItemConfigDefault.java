package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de default para ItemConfig.
 * Referências a raridade e tipo são resolvidas por nome durante a inicialização.
 */
public record ItemConfigDefault(
        String nome,
        String raridadeNome,
        String tipoNome,
        BigDecimal peso,
        Integer valor,
        Integer duracaoPadrao,
        int nivelMinimo,
        String propriedades,
        int ordemExibicao,
        List<ItemEfeitoDefault> efeitos
) {}
