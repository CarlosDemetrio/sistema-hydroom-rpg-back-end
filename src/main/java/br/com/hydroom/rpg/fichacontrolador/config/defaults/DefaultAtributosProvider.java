package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.AtributoConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultAtributosProvider {

    public List<AtributoConfigDTO> get() {
        return List.of(
            AtributoConfigDTO.of("Força",        "FOR",  "Capacidade física bruta, determina capacidade de carga",    "total * 3",         "kg",          1, 120, 1),
            AtributoConfigDTO.of("Agilidade",    "AGI",  "Velocidade e reflexos, determina deslocamento",             "total / 3",         "metros",      1, 120, 2),
            AtributoConfigDTO.of("Vigor",        "VIG",  "Resistência física, redução de dano físico",                "total / 10",        "RD",          1, 120, 3),
            AtributoConfigDTO.of("Sabedoria",    "SAB",  "Resistência mágica, redução de dano mágico",                "total / 10",        "RDM",         1, 120, 4),
            AtributoConfigDTO.of("Intuição",     "INTU", "Sorte e percepção instintiva, pontos de sorte",             "min(total / 20, 3)", "pontos",      1, 120, 5),
            AtributoConfigDTO.of("Inteligência", "INT",  "Capacidade de comando e raciocínio",                        "total / 20",        "comando",     1, 120, 6),
            AtributoConfigDTO.of("Astúcia",      "AST",  "Pensamento estratégico e tático",                           "total / 10",        "estratégia",  1, 120, 7)
        );
    }
}
