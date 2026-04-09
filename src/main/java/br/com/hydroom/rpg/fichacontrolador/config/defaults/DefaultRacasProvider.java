package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.BonusAtributoDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.RacaConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultRacasProvider {

    public List<RacaConfigDTO> getRacas() {
        return List.of(
            RacaConfigDTO.of("Humano",
                "Raca versatil e adaptavel, capaz de aprender qualquer arte ou oficio. Sua maior forca e a adaptabilidade e a capacidade de superacao. Nao possuem vantagens raciais fisicas marcantes, mas compensam com 5 vantagens especiais que refletem a resiliencia e o potencial ilimitado da humanidade.",
                1),
            RacaConfigDTO.of("Karzarcryer",
                "Descendentes de dragoes do plano do fogo, os Karzarcryer possuem escamas ignicas e sangue quente. Bonus: +8 Forca (+24 VIG), -3 Percepcao (-9 INTU). Sao conhecidos por seu temperamento explosivo e pela Ignomia inicial 5. Dominam o elemento fogo e possuem resistencia sobrenatural ao calor.",
                2),
            RacaConfigDTO.of("Ikaruz",
                "Raca de seres alados com afinidade a sabedoria celestial. Possuem asas funcionais e adaptacao a diferentes altitudes. Bonus: +5 Sabedoria, +3 Percepcao (+9 INTU), -3 Resistencia (-9 VIG). Antecedente: Nanismo.",
                3),
            RacaConfigDTO.of("Hankraz",
                "Seres esguios de inteligencia agucada que habitam entre planos de existencia paralelos. Bonus: +5 Inteligencia, +3 Percepcao (+9 INTU), -3 Resistencia (-9 VIG). Antecedente: Baixo vigor.",
                4),
            RacaConfigDTO.of("Atlas",
                "Gigantes de forca incomparavel. Bonus: +8 Forca, -3 Inteligencia. Antecedente: E burro. Sao guerreiros natos mas carecem de sofisticacao intelectual.",
                5),
            RacaConfigDTO.of("Anakarys",
                "Raca agil de predadores naturais com garras, presas e instintos agucados. Bonus: +3 Agilidade, +2 Percepcao (+6 INTU).",
                6)
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
}
