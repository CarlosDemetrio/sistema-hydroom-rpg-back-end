package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.util.List;

/**
 * Variáveis disponíveis para uso em fórmulas de um jogo.
 *
 * @param atributos siglas de atributos configurados no jogo
 * @param bonus     siglas de bônus configurados no jogo
 * @param vantagens siglas de vantagens configuradas no jogo (não nulas)
 * @param fixas     variáveis fixas do sistema (total, nivel, base, custo_base, nivel_vantagem)
 */
public record VariaveisDisponiveisResponse(
    List<SiglaInfoResponse> atributos,
    List<SiglaInfoResponse> bonus,
    List<SiglaInfoResponse> vantagens,
    List<String> fixas
) {}
