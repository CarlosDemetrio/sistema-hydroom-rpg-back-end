package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.util.Map;

/**
 * Resposta resumida de uma ficha de personagem, com valores calculados agregados.
 */
public record FichaResumoResponse(
        Long id,
        String nome,
        int nivel,
        long xp,
        String racaNome,
        String classeNome,
        Map<String, Integer> atributosTotais,
        Map<String, Integer> bonusTotais,
        int vidaTotal,
        int essenciaTotal,
        int ameacaTotal
) {}
