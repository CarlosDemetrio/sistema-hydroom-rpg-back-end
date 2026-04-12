package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.BonusAtributoDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.PontosNivelConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.RacaConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.RacaVantagemPreDefinidaDefault;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Provedor das 6 raças padrão do universo Klayrah com bônus raciais e pontos por nível.
 *
 * <p>Bônus de nível são concedidos em marcos (1, 5, 10, 15, 20, 25, 30, 35).
 * Karzarcryer e Atlas não recebem bônus de nível — seus atributos iniciais já
 * equivalem a muitos pontos de head start.</p>
 */
@Component
public class DefaultRacasProvider {

    private static final List<Integer> MARCOS = List.of(1, 5, 10, 15, 20, 25, 30, 35);

    public List<RacaConfigDTO> getRacas() {
        return List.of(
            RacaConfigDTO.of(
                "Humano",
                "Raça versátil e adaptável, capaz de aprender qualquer arte ou ofício. Sua maior força é a adaptabilidade e a capacidade de superação. Não possuem vantagens raciais físicas marcantes, mas compensam com 5 vantagens especiais que refletem a resiliência e o potencial ilimitado da humanidade.",
                1,
                pontosEmMarcos(0, 1),
                List.of(),
                List.of(
                    vantagem("Adaptabilidade Humana", "VAHU"),
                    vantagem("Resiliência Humana", "VRHU"),
                    vantagem("Versatilidade Humana", "VVHU"),
                    vantagem("Espírito Inabalável", "VEIN"),
                    vantagem("Legado de Civilização", "VLCI")
                )
            ),
            RacaConfigDTO.of(
                "Karzarcryer",
                "Descendentes de dragões do plano do fogo, os Karzarcryer possuem escamas ígnicas e sangue quente. Bônus: +8 Resistência (+24 VIG), -3 Percepção (-9 INTU). São conhecidos por seu temperamento explosivo (Antecedente: Falta de autocontrole) e pela Ignomia inicial 5, que representa sua reputação de destruição instintiva. Dominam o elemento fogo e possuem resistência sobrenatural ao calor.",
                2,
                List.of(),
                List.of(),
                List.of(
                    vantagem("Elemento Natural: Fogo", "VENF"),
                    vantagem("Imunidade Elemental: Fogo", "VIEF"),
                    vantagem("Estômago de Dragão", "VESD")
                )
            ),
            RacaConfigDTO.of(
                "Ikarúz",
                "Raça de seres alados com afinidade à sabedoria celestial. Possuem asas funcionais e adaptação a diferentes altitudes. Bônus: +5 Sabedoria (+5 SAB), +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Nanismo — apesar das asas grandiosas, seu corpo é pequeno e delicado, tornando-os vulneráveis em combate corpo a corpo.",
                3,
                pontosEmMarcos(0, 1),
                List.of(),
                List.of(
                    vantagem("Membro Adicional: Asas", "VASA"),
                    vantagem("Adaptação Atmosférica", "VADA"),
                    vantagem("Combate Alado", "VCAL")
                )
            ),
            RacaConfigDTO.of(
                "Hankráz",
                "Seres esguios de inteligência aguçada que habitam entre planos de existência paralelos. Seus piercings mágicos amplificam capacidades sobrenaturais. Bônus: +5 Inteligência (+5 INT), +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Baixo vigor — corpos frágeis que compensam com mente analítica e habilidades multidimensionais.",
                4,
                pontosEmMarcos(0, 1),
                List.of(),
                List.of(
                    vantagem("Piercings Raciais", "VPIR"),
                    vantagem("Corpo Esguio", "VCEG"),
                    vantagem("Vagante entre Mundos", "VVEM")
                )
            ),
            RacaConfigDTO.of(
                "Atlas",
                "Gigantes de força incomparável com quatro braços plenamente funcionais, os Atlas são a raça mais fisicamente poderosa de Klayrah. Bônus: +8 Força (+8 FOR), -3 Inteligência (-3 INT). Antecedente: É burro — já representado pela penalidade em INT. São guerreiros natos que usam seus dois pares de braços para empunhar múltiplas armas ou escudos simultaneamente, mas carecem de sofisticação intelectual. Vantagens raciais: Membros Adicionais: Braços (que fundamenta Ambidestria e Ataque Adicional nativos), Capacidade de Força Máxima inata.",
                5,
                List.of(),
                List.of(),
                List.of(
                    vantagem("Membros Adicionais: Braços", "VMAB"),
                    vantagem("Capacidade de Força Máxima", "VCFM"),
                    vantagem("Ambidestria", "VAMB"),
                    vantagem("Ataque Adicional", "VAA")
                )
            ),
            RacaConfigDTO.of(
                "Anakarys",
                "Raça ágil de predadores naturais com garras, presas e instintos aguçados. Bônus: +3 Agilidade (+3 AGI), +2 Percepção (+6 INTU). Possuem armas naturais aprimoradas, formas únicas de deslocamento e um ataque adicional racial com restrições específicas de uso.",
                6,
                pontosEmMarcos(0, 1),
                List.of(),
                List.of(
                    vantagem("Armas Naturais Aprimoradas", "VANA"),
                    vantagem("Deslocamento Especial", "VDES"),
                    vantagem("Ataque Adicional Racial", "VAAR")
                )
            )
        );
    }

    public Map<String, List<BonusAtributoDTO>> getBonusRaciais() {
        return Map.ofEntries(
            Map.entry("Humano",      List.of()),
            Map.entry("Karzarcryer", List.of(
                BonusAtributoDTO.of("VIG",   24),
                BonusAtributoDTO.of("INTU",  -9)
            )),
            Map.entry("Ikarúz", List.of(
                BonusAtributoDTO.of("SAB",   5),
                BonusAtributoDTO.of("INTU",  9),
                BonusAtributoDTO.of("VIG",  -9)
            )),
            Map.entry("Hankráz", List.of(
                BonusAtributoDTO.of("INT",   5),
                BonusAtributoDTO.of("INTU",  9),
                BonusAtributoDTO.of("VIG",  -9)
            )),
            Map.entry("Atlas", List.of(
                BonusAtributoDTO.of("FOR",   8),
                BonusAtributoDTO.of("INT",  -3)
            )),
            Map.entry("Anakarys", List.of(
                BonusAtributoDTO.of("AGI",   3),
                BonusAtributoDTO.of("INTU",  6)
            ))
        );
    }

    /** +X pontos nos marcos de nível 1, 5, 10, 15, 20, 25, 30, 35. */
    private static List<PontosNivelConfigDTO> pontosEmMarcos(int pa, int pv) {
        return MARCOS.stream()
                .map(n -> new PontosNivelConfigDTO(n, pa, pv))
                .toList();
    }

    private static RacaVantagemPreDefinidaDefault vantagem(String vantagemNome) {
        return vantagem(vantagemNome, null);
    }

    private static RacaVantagemPreDefinidaDefault vantagem(String vantagemNome, String vantagemSigla) {
        return new RacaVantagemPreDefinidaDefault(vantagemNome, vantagemSigla, 1);
    }
}
