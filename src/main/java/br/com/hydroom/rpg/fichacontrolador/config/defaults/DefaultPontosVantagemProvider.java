package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.PontosVantagemConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultPontosVantagemProvider {

    public List<PontosVantagemConfigDTO> get() {
        return List.of(
            PontosVantagemConfigDTO.of(1,  6),
            PontosVantagemConfigDTO.of(2,  0),
            PontosVantagemConfigDTO.of(3,  0),
            PontosVantagemConfigDTO.of(4,  0),
            PontosVantagemConfigDTO.of(5,  3),
            PontosVantagemConfigDTO.of(6,  0),
            PontosVantagemConfigDTO.of(7,  0),
            PontosVantagemConfigDTO.of(8,  0),
            PontosVantagemConfigDTO.of(9,  0),
            PontosVantagemConfigDTO.of(10, 10),
            PontosVantagemConfigDTO.of(11, 0),
            PontosVantagemConfigDTO.of(12, 0),
            PontosVantagemConfigDTO.of(13, 0),
            PontosVantagemConfigDTO.of(14, 0),
            PontosVantagemConfigDTO.of(15, 3),
            PontosVantagemConfigDTO.of(16, 0),
            PontosVantagemConfigDTO.of(17, 0),
            PontosVantagemConfigDTO.of(18, 0),
            PontosVantagemConfigDTO.of(19, 0),
            PontosVantagemConfigDTO.of(20, 10),
            PontosVantagemConfigDTO.of(21, 0),
            PontosVantagemConfigDTO.of(22, 0),
            PontosVantagemConfigDTO.of(23, 0),
            PontosVantagemConfigDTO.of(24, 0),
            PontosVantagemConfigDTO.of(25, 3),
            PontosVantagemConfigDTO.of(26, 0),
            PontosVantagemConfigDTO.of(27, 0),
            PontosVantagemConfigDTO.of(28, 0),
            PontosVantagemConfigDTO.of(29, 0),
            PontosVantagemConfigDTO.of(30, 15),
            PontosVantagemConfigDTO.of(31, 0),
            PontosVantagemConfigDTO.of(32, 0),
            PontosVantagemConfigDTO.of(33, 0),
            PontosVantagemConfigDTO.of(34, 0),
            PontosVantagemConfigDTO.of(35, 3)
        );
    }
}
