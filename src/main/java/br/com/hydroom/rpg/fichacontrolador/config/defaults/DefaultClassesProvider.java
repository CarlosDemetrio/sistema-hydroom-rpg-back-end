package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ClasseConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.PontosNivelConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Provedor das 12 classes de personagem padrão com seus bônus de pontos por nível.
 *
 * <p>Bônus são ADICIONAIS ao pool global do NivelConfig (3 atributo + 1 aptidão/nível).
 * O campo {@code pontosVantagem} corresponde ao antigo conceito de {@code pontosAptidao}
 * por classe (removido da entidade por decisão PO 2026-04-04 — aptidões são globais).</p>
 *
 * <p>Padrões de bônus:
 * <ul>
 *   <li>Físicas puras (Guerreiro, Berserker): +1 atributo/nível</li>
 *   <li>Físicas mistas (Arqueiro, Monge): +1 atributo nos marcos 5/10/.../35</li>
 *   <li>Arcanas/mentais + furtivas + suporte: +1 vantagem/nível</li>
 * </ul>
 * </p>
 */
@Component
public class DefaultClassesProvider {

    public List<ClasseConfigDTO> get() {
        return List.of(
            ClasseConfigDTO.of("Guerreiro",       "Especialista em combate corpo a corpo",           1, pontosPorNivel(1, 0)),
            ClasseConfigDTO.of("Arqueiro",         "Mestre em combate à distância",                  2, pontosEmMarcos5a35(1, 0)),
            ClasseConfigDTO.of("Monge",            "Lutador desarmado com disciplina espiritual",    3, pontosEmMarcos5a35(1, 0)),
            ClasseConfigDTO.of("Berserker",        "Guerreiro selvagem de fúria incontrolável",      4, pontosPorNivel(1, 0)),
            ClasseConfigDTO.of("Assassino",        "Especialista em ataques furtivos e letais",      5, pontosPorNivel(0, 1)),
            ClasseConfigDTO.of("Fauno (Herdeiro)", "Herdeiro com poderes especiais",                 6, pontosPorNivel(0, 1)),
            ClasseConfigDTO.of("Mago",             "Conjurador de magias arcanas",                   7, pontosPorNivel(0, 1)),
            ClasseConfigDTO.of("Feiticeiro",       "Usuário de magia inata",                         8, pontosPorNivel(0, 1)),
            ClasseConfigDTO.of("Necromante",       "Manipulador de forças da morte",                 9, pontosPorNivel(0, 1)),
            ClasseConfigDTO.of("Sacerdote",        "Servo divino com poderes sagrados",             10, pontosPorNivel(0, 1)),
            ClasseConfigDTO.of("Ladrão",           "Especialista em subterfúgio e furto",           11, pontosPorNivel(0, 1)),
            ClasseConfigDTO.of("Negociante",       "Mestre em comércio e persuasão",                12, pontosPorNivel(0, 1))
        );
    }

    /** +X pontos por cada nível 1..35. */
    private static List<PontosNivelConfigDTO> pontosPorNivel(int pa, int pv) {
        return IntStream.rangeClosed(1, 35)
                .mapToObj(n -> new PontosNivelConfigDTO(n, pa, pv))
                .toList();
    }

    /** +X pontos nos marcos de disciplina física: níveis 5, 10, 15, 20, 25, 30, 35. */
    private static List<PontosNivelConfigDTO> pontosEmMarcos5a35(int pa, int pv) {
        return List.of(5, 10, 15, 20, 25, 30, 35).stream()
                .map(n -> new PontosNivelConfigDTO(n, pa, pv))
                .toList();
    }
}
