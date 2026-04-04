package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO para configuração padrão de CategoriaVantagem.
 * Usado pelo GameDefaultConfigProvider para inicializar categorias de vantagem de um jogo.
 */
public record CategoriaVantagemDTO(
        String nome,
        String cor,
        int ordemExibicao
) {
    public static CategoriaVantagemDTO of(String nome, String cor, int ordemExibicao) {
        return new CategoriaVantagemDTO(nome, cor, ordemExibicao);
    }
}
