package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO imutável para um entrada de pontos extras por nível (classe ou raça).
 *
 * <p>Tanto {@code pontosAtributo} quanto {@code pontosVantagem} são adicionais
 * ao pool global definido em {@code NivelConfig}.</p>
 *
 * <p>Nota: {@code pontosAptidao} foi removido por decisão do PO (2026-04-04) —
 * aptidões são globais e independentes de classe/raça.</p>
 */
public record PontosNivelConfigDTO(
        int nivel,
        int pontosAtributo,
        int pontosVantagem
) {}
