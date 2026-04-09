package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO para configuração padrão de BonusConfig (B.B.A, B.B.M, Defesa, etc.).
 * Usado pelo GameDefaultConfigProvider para inicializar bônus calculados de um jogo.
 */
public record BonusConfigDTO(
        String nome,
        String sigla,
        String formulaBase,
        String descricao,
        int ordemExibicao
) {
    public static BonusConfigDTO of(String nome, String sigla, String formulaBase, String descricao, int ordemExibicao) {
        return new BonusConfigDTO(nome, sigla, formulaBase, descricao, ordemExibicao);
    }

    public static BonusConfigDTO of(String nome, String sigla, String formulaBase, int ordemExibicao) {
        return new BonusConfigDTO(nome, sigla, formulaBase, null, ordemExibicao);
    }
}
