package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ClasseAptidaoBonusDefault;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ClasseBonusDefault;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ClasseConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ClasseVantagemPreDefinidaDefault;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.PontosNivelConfigDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Provedor das 12 classes de personagem padrão sincronizado com CSVs canônicos.
 *
 * <p>Fonte: docs/revisao-game-default/csv/
 * <ul>
 *   <li>15-classe-personagem.csv — metadados base</li>
 *   <li>15b-classe-bonus.csv — bônus derivados</li>
 *   <li>15c-classe-aptidao-bonus.csv — aptidões bônus</li>
 *   <li>15d-classe-pontos-config.csv — pontos por nível</li>
 *   <li>15e-classe-vantagem-predefinida.csv — vantagens iniciais</li>
 * </ul>
 * </p>
 */
@Component
public class DefaultClassesProvider {

    public List<ClasseConfigDTO> get() {
        return List.of(
            guerreiro(),
            arqueiro(),
            monge(),
            berserker(),
            assassino(),
            faunoHerdeiro(),
            mago(),
            feiticeiro(),
            necromante(),
            sacerdote(),
            ladrao(),
            negociante()
        );
    }

    private ClasseConfigDTO guerreiro() {
        return ClasseConfigDTO.of(
            "Guerreiro",
            "Especialista em combate corpo a corpo",
            1,
            pontosPorNivel(1, 0),
            List.of(
                new ClasseBonusDefault("B.B.A", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Bloqueio", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Defesa", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Guarda", 3, 1),
                new ClasseAptidaoBonusDefault("Aparar", 3, 2),
                new ClasseAptidaoBonusDefault("Atletismo", 2, 3),
                new ClasseAptidaoBonusDefault("Resistência", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Ofensivo", null, 1),
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Defensivo", null, 1)
            )
        );
    }

    private ClasseConfigDTO arqueiro() {
        return ClasseConfigDTO.of(
            "Arqueiro",
            "Mestre em combate à distância",
            2,
            pontosEmMarcos5a35(1, 0),
            List.of(
                new ClasseBonusDefault("B.B.A", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Reflexo", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Percepção", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Perseguição", 3, 1),
                new ClasseAptidaoBonusDefault("Observação", 3, 2),
                new ClasseAptidaoBonusDefault("Atletismo", 2, 3),
                new ClasseAptidaoBonusDefault("Resvalar", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Ofensivo", null, 1),
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Evasivo", null, 1),
                new ClasseVantagemPreDefinidaDefault("Memória Fotográfica", null, 1)
            )
        );
    }

    private ClasseConfigDTO monge() {
        return ClasseConfigDTO.of(
            "Monge",
            "Lutador desarmado com disciplina espiritual",
            3,
            pontosEmMarcos5a35(1, 0),
            List.of(
                new ClasseBonusDefault("B.B.A", new BigDecimal("1"), 1),
                new ClasseBonusDefault("Reflexo", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Esquiva", new BigDecimal("2"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Auto Controle", 4, 1),
                new ClasseAptidaoBonusDefault("Arte da Fuga", 3, 2),
                new ClasseAptidaoBonusDefault("Atletismo", 3, 3),
                new ClasseAptidaoBonusDefault("Acrobacia", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Evasivo", null, 2),
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Ofensivo", null, 1),
                new ClasseVantagemPreDefinidaDefault("Concentração", null, 1)
            )
        );
    }

    private ClasseConfigDTO berserker() {
        return ClasseConfigDTO.of(
            "Berserker",
            "Guerreiro selvagem de fúria incontrolável",
            4,
            pontosPorNivel(1, 0),
            List.of(
                new ClasseBonusDefault("B.B.A", new BigDecimal("3"), 1),
                new ClasseBonusDefault("Iniciativa", new BigDecimal("1"), 2),
                new ClasseBonusDefault("Bloqueio", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Atletismo", 4, 1),
                new ClasseAptidaoBonusDefault("Resistência", 3, 2),
                new ClasseAptidaoBonusDefault("Resvalar", 2, 3),
                new ClasseAptidaoBonusDefault("Perseguição", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Ofensivo", null, 2),
                new ClasseVantagemPreDefinidaDefault("Saúde de Ferro", null, 1)
            )
        );
    }

    private ClasseConfigDTO assassino() {
        return ClasseConfigDTO.of(
            "Assassino",
            "Especialista em ataques furtivos e letais",
            5,
            pontosPorNivel(0, 1),
            List.of(
                new ClasseBonusDefault("B.B.A", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Reflexo", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Esquiva", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Furtividade", 4, 1),
                new ClasseAptidaoBonusDefault("Prestidigitação", 3, 2),
                new ClasseAptidaoBonusDefault("Arte da Fuga", 2, 3),
                new ClasseAptidaoBonusDefault("Resvalar", 2, 4),
                new ClasseAptidaoBonusDefault("Blefar", 1, 5)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Ofensivo", null, 1),
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Evasivo", null, 1),
                new ClasseVantagemPreDefinidaDefault("Saque Rápido", null, 1)
            )
        );
    }

    private ClasseConfigDTO faunoHerdeiro() {
        return ClasseConfigDTO.of(
            "Fauno (Herdeiro)",
            "Herdeiro com poderes especiais",
            6,
            pontosPorNivel(0, 1),
            List.of(
                new ClasseBonusDefault("B.B.M", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Percepção", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Raciocínio", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Sentir Motivação", 3, 1),
                new ClasseAptidaoBonusDefault("Observação", 3, 2),
                new ClasseAptidaoBonusDefault("Auto Controle", 2, 3),
                new ClasseAptidaoBonusDefault("Prontidão", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento Mágico", null, 1),
                new ClasseVantagemPreDefinidaDefault("Treinamento em Percepção Mágica", null, 1),
                new ClasseVantagemPreDefinidaDefault("Herança", null, 1)
            )
        );
    }

    private ClasseConfigDTO mago() {
        return ClasseConfigDTO.of(
            "Mago",
            "Conjurador de magias arcanas",
            7,
            pontosPorNivel(0, 1),
            List.of(
                new ClasseBonusDefault("B.B.M", new BigDecimal("3"), 1),
                new ClasseBonusDefault("Raciocínio", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Percepção", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Investigar", 3, 1),
                new ClasseAptidaoBonusDefault("Idiomas", 3, 2),
                new ClasseAptidaoBonusDefault("Observação", 2, 3),
                new ClasseAptidaoBonusDefault("Prontidão", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento Mágico", null, 2),
                new ClasseVantagemPreDefinidaDefault("Treinamento Lógico", null, 1),
                new ClasseVantagemPreDefinidaDefault("Concentração", null, 1)
            )
        );
    }

    private ClasseConfigDTO feiticeiro() {
        return ClasseConfigDTO.of(
            "Feiticeiro",
            "Usuário de magia inata",
            8,
            pontosPorNivel(0, 1),
            List.of(
                new ClasseBonusDefault("B.B.M", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Iniciativa", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Reflexo", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Prontidão", 3, 1),
                new ClasseAptidaoBonusDefault("Auto Controle", 2, 2),
                new ClasseAptidaoBonusDefault("Observação", 2, 3),
                new ClasseAptidaoBonusDefault("Sentir Motivação", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento Mágico", null, 1),
                new ClasseVantagemPreDefinidaDefault("Treinamento em Percepção Mágica", null, 1),
                new ClasseVantagemPreDefinidaDefault("Concentração", null, 1)
            )
        );
    }

    private ClasseConfigDTO necromante() {
        return ClasseConfigDTO.of(
            "Necromante",
            "Manipulador de forças da morte",
            9,
            pontosPorNivel(0, 1),
            List.of(
                new ClasseBonusDefault("B.B.M", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Raciocínio", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Percepção", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Investigar", 3, 1),
                new ClasseAptidaoBonusDefault("Falsificar", 3, 2),
                new ClasseAptidaoBonusDefault("Prontidão", 2, 3),
                new ClasseAptidaoBonusDefault("Sobrevivência", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento Mágico", null, 1),
                new ClasseVantagemPreDefinidaDefault("Treinamento Lógico", null, 1),
                new ClasseVantagemPreDefinidaDefault("Vínculo com Organização", null, 1)
            )
        );
    }

    private ClasseConfigDTO sacerdote() {
        return ClasseConfigDTO.of(
            "Sacerdote",
            "Servo divino com poderes sagrados",
            10,
            pontosPorNivel(0, 1),
            List.of(
                new ClasseBonusDefault("B.B.M", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Defesa", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Raciocínio", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Diplomacia", 3, 1),
                new ClasseAptidaoBonusDefault("Auto Controle", 3, 2),
                new ClasseAptidaoBonusDefault("Sentir Motivação", 3, 3),
                new ClasseAptidaoBonusDefault("Observação", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento Mágico", null, 1),
                new ClasseVantagemPreDefinidaDefault("Treinamento em Manipulação", null, 1),
                new ClasseVantagemPreDefinidaDefault("Saúde de Ferro", null, 1)
            )
        );
    }

    private ClasseConfigDTO ladrao() {
        return ClasseConfigDTO.of(
            "Ladrão",
            "Especialista em subterfúgio e furto",
            11,
            pontosPorNivel(0, 1),
            List.of(
                new ClasseBonusDefault("Esquiva", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Reflexo", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Iniciativa", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Furtividade", 3, 1),
                new ClasseAptidaoBonusDefault("Prestidigitação", 3, 2),
                new ClasseAptidaoBonusDefault("Arte da Fuga", 3, 3),
                new ClasseAptidaoBonusDefault("Operação de Mecanismos", 2, 4),
                new ClasseAptidaoBonusDefault("Blefar", 2, 5)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento em Combate Evasivo", null, 1),
                new ClasseVantagemPreDefinidaDefault("Saque Rápido", null, 2),
                new ClasseVantagemPreDefinidaDefault("Ofícios", null, 1)
            )
        );
    }

    private ClasseConfigDTO negociante() {
        return ClasseConfigDTO.of(
            "Negociante",
            "Mestre em comércio e persuasão",
            12,
            pontosPorNivel(0, 1),
            List.of(
                new ClasseBonusDefault("Raciocínio", new BigDecimal("2"), 1),
                new ClasseBonusDefault("Percepção", new BigDecimal("2"), 2),
                new ClasseBonusDefault("Iniciativa", new BigDecimal("1"), 3)
            ),
            List.of(
                new ClasseAptidaoBonusDefault("Diplomacia", 4, 1),
                new ClasseAptidaoBonusDefault("Blefar", 3, 2),
                new ClasseAptidaoBonusDefault("Sentir Motivação", 3, 3),
                new ClasseAptidaoBonusDefault("Atuação", 2, 4)
            ),
            List.of(
                new ClasseVantagemPreDefinidaDefault("Treinamento em Manipulação", null, 1),
                new ClasseVantagemPreDefinidaDefault("Ofícios", null, 2),
                new ClasseVantagemPreDefinidaDefault("Vínculo com Organização", null, 1)
            )
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
