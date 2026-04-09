package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.BonusConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultBonusProvider {

    public List<BonusConfigDTO> get() {
        return List.of(
            BonusConfigDTO.of("B.B.A",      "BBA", "(FOR + AGI) / 3", "Bônus base de ataque físico, derivado da Força e Agilidade do personagem",                       1),
            BonusConfigDTO.of("B.B.M",      "BBM", "(SAB + INT) / 3", "Bônus base de ação mental ou mágica, derivado da Sabedoria e Inteligência",                      2),
            BonusConfigDTO.of("Defesa",     "DEF", "VIG / 5",         "Redução passiva de dano físico recebido, derivada do Vigor",                                      3),
            BonusConfigDTO.of("Esquiva",    "ESQ", "AGI / 5",         "Valor de referência para desviar de ataques, derivado da Agilidade",                              4),
            BonusConfigDTO.of("Iniciativa", "INI", "INTU / 5",        "Determina a ordem de ação em combate, derivada da Intuição",                                      5),
            BonusConfigDTO.of("Percepção",  "PER", "INTU / 3",        "Capacidade de notar detalhes, ameaças e pistas no ambiente, derivada da Intuição",                6),
            BonusConfigDTO.of("Raciocínio", "RAC", "INT / 3",         "Qualidade do pensamento analítico e resolução de problemas, derivado da Inteligência",            7),
            BonusConfigDTO.of("Bloqueio",   "BLO", "VIG / 3",         "Capacidade de absorver impactos com escudo ou arma, derivado do Vigor",                          8),
            BonusConfigDTO.of("Reflexo",    "REF", "AGI / 3",         "Velocidade de reação a eventos imprevistos ou ataques surpresa, derivado da Agilidade",           9)
        );
    }
}
