package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO para configuração padrão de PontosVantagemConfig (pontos de vantagem por nível).
 * Usado pelo GameDefaultConfigProvider para inicializar pontos de vantagem de um jogo.
 */
public record PontosVantagemConfigDTO(
        int nivel,
        int pontos
) {
    public static PontosVantagemConfigDTO of(int nivel, int pontos) {
        return new PontosVantagemConfigDTO(nivel, pontos);
    }
}
