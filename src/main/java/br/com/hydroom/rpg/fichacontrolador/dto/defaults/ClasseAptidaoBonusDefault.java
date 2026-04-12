package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO default para bônus fixos de aptidão concedidos por classe.
 * A referência à aptidão é feita por nome e resolvida durante a inicialização.
 */
public record ClasseAptidaoBonusDefault(
        String aptidaoNome,
        Integer bonus,
        Integer ordemExibicao
) {}
