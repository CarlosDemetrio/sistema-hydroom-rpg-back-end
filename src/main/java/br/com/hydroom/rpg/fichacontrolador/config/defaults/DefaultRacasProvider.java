package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.BonusAtributoDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.PontosNivelConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.RacaConfigDTO;
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
            RacaConfigDTO.of("Humano",
                "Raça versátil e adaptável, capaz de aprender qualquer arte ou ofício. Sua maior força é a adaptabilidade e a capacidade de superação. Não possuem vantagens raciais físicas marcantes, mas compensam com 5 vantagens especiais que refletem a resiliência e o potencial ilimitado da humanidade.",
                1, pontosEmMarcos(0, 1)),
            RacaConfigDTO.of("Karzarcryer",
                "Descendentes de dragões do plano do fogo, os Karzarcryer possuem escamas ígneas e sangue quente. Bônus: +8 Força (+24 VIG), -3 Percepção (-9 INTU). São conhecidos por seu temperamento explosivo e pela Ignomia inicial 5. Dominam o elemento fogo e possuem resistência sobrenatural ao calor.",
                2, List.of()),
            RacaConfigDTO.of("Ikaruz",
                "Raça de seres alados com afinidade à sabedoria celestial. Possuem asas funcionais e adaptação a diferentes altitudes. Bônus: +5 Sabedoria, +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Nanismo.",
                3, pontosEmMarcos(0, 1)),
            RacaConfigDTO.of("Hankraz",
                "Seres esguios de inteligência aguçada que habitam entre planos de existência paralelos. Bônus: +5 Inteligência, +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Baixo vigor.",
                4, pontosEmMarcos(0, 1)),
            RacaConfigDTO.of("Atlas",
                "Gigantes de força incomparável. Bônus: +8 Força, -3 Inteligência. Antecedente: É burro. São guerreiros natos mas carecem de sofisticação intelectual.",
                5, List.of()),
            RacaConfigDTO.of("Anakarys",
                "Raça ágil de predadores naturais com garras, presas e instintos aguçados. Bônus: +3 Agilidade, +2 Percepção (+6 INTU).",
                6, pontosEmMarcos(0, 1))
        );
    }

    public Map<String, List<BonusAtributoDTO>> getBonusRaciais() {
        return Map.ofEntries(
            Map.entry("Humano",      List.of()),
            Map.entry("Karzarcryer", List.of(
                BonusAtributoDTO.of("VIG",   24),
                BonusAtributoDTO.of("INTU",  -9)
            )),
            Map.entry("Ikaruz", List.of(
                BonusAtributoDTO.of("SAB",   5),
                BonusAtributoDTO.of("INTU",  9),
                BonusAtributoDTO.of("VIG",  -9)
            )),
            Map.entry("Hankraz", List.of(
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
}
