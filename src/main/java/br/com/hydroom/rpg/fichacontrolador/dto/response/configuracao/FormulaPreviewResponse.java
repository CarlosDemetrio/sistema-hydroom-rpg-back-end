package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.util.List;
import java.util.Set;

/**
 * Resultado do preview/teste de uma fórmula.
 *
 * @param valida          true se a fórmula é válida
 * @param resultado       valor calculado (null se inválida)
 * @param erros           mensagens de erro (vazio se válida)
 * @param variaveisUsadas variáveis reconhecidas na fórmula
 */
public record FormulaPreviewResponse(
    boolean valida,
    Double resultado,
    List<String> erros,
    Set<String> variaveisUsadas
) {}
