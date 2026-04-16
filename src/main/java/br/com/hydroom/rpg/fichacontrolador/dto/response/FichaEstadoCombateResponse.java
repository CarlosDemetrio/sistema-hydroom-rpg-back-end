package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.util.List;

/**
 * Estado de combate de uma ficha: vida, essência e dano por membro do corpo.
 *
 * <p>Fonte autoritativa para a aba Sessão do frontend — retorna os valores
 * persistidos sem recalcular atributos derivados.</p>
 */
public record FichaEstadoCombateResponse(
        int vidaAtual,
        int vidaTotal,
        int essenciaAtual,
        int essenciaTotal,
        List<MembroEstadoResponse> membros
) {

    public record MembroEstadoResponse(
            Long membroCorpoConfigId,
            String nome,
            int vida,
            int danoRecebido,
            int vidaRestante
    ) {}
}
