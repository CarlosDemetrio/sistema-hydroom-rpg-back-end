package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ClasseConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultClassesProvider {

    public List<ClasseConfigDTO> get() {
        return List.of(
            ClasseConfigDTO.of("Guerreiro",        "Especialista em combate corpo a corpo",        1),
            ClasseConfigDTO.of("Arqueiro",          "Mestre em combate a distancia",               2),
            ClasseConfigDTO.of("Monge",             "Lutador desarmado com disciplina espiritual", 3),
            ClasseConfigDTO.of("Berserker",         "Guerreiro selvagem de furia incontrolavel",   4),
            ClasseConfigDTO.of("Assassino",         "Especialista em ataques furtivos e letais",   5),
            ClasseConfigDTO.of("Fauno (Herdeiro)",  "Herdeiro com poderes especiais",              6),
            ClasseConfigDTO.of("Mago",              "Conjurador de magias arcanas",                7),
            ClasseConfigDTO.of("Feiticeiro",        "Usuario de magia inata",                      8),
            ClasseConfigDTO.of("Necromante",        "Manipulador de forcas da morte",              9),
            ClasseConfigDTO.of("Sacerdote",         "Servo divino com poderes sagrados",           10),
            ClasseConfigDTO.of("Ladrao",            "Especialista em subterfugio e furto",         11),
            ClasseConfigDTO.of("Negociante",        "Mestre em comercio e persuasao",              12)
        );
    }
}
