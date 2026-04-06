package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.util.Map;

/**
 * Resposta resumida de uma ficha de personagem, com valores calculados agregados.
 *
 * <p>Inclui pontos disponiveis para atributos, aptidoes e vantagens
 * (total concedido pelo nivel - pontos ja gastos).</p>
 *
 * <p>Este endpoint é a fonte autoritativa para vidaAtual, essenciaAtual e os totais calculados.
 * O frontend deve usar /resumo para obter esses valores em vez do GET /fichas/{id} básico.</p>
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
        int vidaAtual,
        int vidaTotal,
        int essenciaAtual,
        int essenciaTotal,
        int ameacaTotal,
        int pontosAtributoDisponiveis,
        int pontosAptidaoDisponiveis,
        int pontosVantagemDisponiveis
) {}
