package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.GeneroConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.IndoleConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.MembroCorpoConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.PresencaConfigDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DefaultConfigSimpleProvider {

    public List<GeneroConfigDTO> getGeneros() {
        return List.of(
            GeneroConfigDTO.of("Masculino", "Personagem de identidade masculina",                              1),
            GeneroConfigDTO.of("Feminino",  "Personagem de identidade feminina",                               2),
            GeneroConfigDTO.of("Outro",     "Personagem com identidade de gênero não binária ou indefinida",   3)
        );
    }

    public List<IndoleConfigDTO> getIndoles() {
        return List.of(
            IndoleConfigDTO.of("Bom",    "Movido por compaixão e altruísmo, tende a ajudar os necessitados e defender os fracos",  1),
            IndoleConfigDTO.of("Mau",    "Guiado por ambição cruel ou egoísmo, usa os outros como meios para seus próprios fins",  2),
            IndoleConfigDTO.of("Neutro", "Sem viés moral definido, age conforme as circunstâncias, nem bondoso nem cruel",         3)
        );
    }

    public List<PresencaConfigDTO> getPresencas() {
        return List.of(
            PresencaConfigDTO.of("Bom",     "Aura de benevolência e proteção, aqueles próximos sentem conforto e confiança",         1),
            PresencaConfigDTO.of("Leal",    "Aura de ordem e autoridade, transmite disciplina e respeito pelas leis e hierarquias",  2),
            PresencaConfigDTO.of("Caótico", "Aura imprevisível e perturbadora, semeia instabilidade e desconforto ao redor",         3),
            PresencaConfigDTO.of("Neutro",  "Aura equilibrada, sem inclinação evidente, passa despercebido pela maioria",            4)
        );
    }

    public List<MembroCorpoConfigDTO> getMembrosCorpo() {
        return List.of(
            MembroCorpoConfigDTO.of("Cabeça",         new BigDecimal("0.75"), 1),
            MembroCorpoConfigDTO.of("Tronco",         new BigDecimal("0.35"), 2),
            MembroCorpoConfigDTO.of("Braço Direito",  new BigDecimal("0.10"), 3),
            MembroCorpoConfigDTO.of("Braço Esquerdo", new BigDecimal("0.10"), 4),
            MembroCorpoConfigDTO.of("Perna Direita",  new BigDecimal("0.10"), 5),
            MembroCorpoConfigDTO.of("Perna Esquerda", new BigDecimal("0.10"), 6),
            MembroCorpoConfigDTO.of("Sangue",         new BigDecimal("1.00"), 9)
        );
    }
}
