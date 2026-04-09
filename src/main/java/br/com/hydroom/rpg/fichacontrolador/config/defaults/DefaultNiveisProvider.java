package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.LimitadorConfigDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.NivelConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultNiveisProvider {

    public List<NivelConfigDTO> getNiveis() {
        return List.of(
            NivelConfigDTO.of(0,  0L,      0, 0, 0, 10),
            NivelConfigDTO.of(1,  1000L,   3, 1, 3, 10),
            NivelConfigDTO.of(2,  3000L,   3, 1, 3, 50),
            NivelConfigDTO.of(3,  6000L,   3, 1, 3, 50),
            NivelConfigDTO.of(4,  10000L,  3, 1, 3, 50),
            NivelConfigDTO.of(5,  15000L,  3, 1, 3, 50),
            NivelConfigDTO.of(6,  21000L,  3, 1, 3, 50),
            NivelConfigDTO.of(7,  28000L,  3, 1, 3, 50),
            NivelConfigDTO.of(8,  36000L,  3, 1, 3, 50),
            NivelConfigDTO.of(9,  45000L,  3, 1, 3, 50),
            NivelConfigDTO.of(10, 55000L,  3, 1, 3, 50),
            NivelConfigDTO.of(11, 66000L,  3, 1, 3, 50),
            NivelConfigDTO.of(12, 78000L,  3, 1, 3, 50),
            NivelConfigDTO.of(13, 91000L,  3, 1, 3, 50),
            NivelConfigDTO.of(14, 105000L, 3, 1, 3, 50),
            NivelConfigDTO.of(15, 120000L, 3, 1, 3, 50),
            NivelConfigDTO.of(16, 136000L, 3, 1, 3, 50),
            NivelConfigDTO.of(17, 153000L, 3, 1, 3, 50),
            NivelConfigDTO.of(18, 171000L, 3, 1, 3, 50),
            NivelConfigDTO.of(19, 190000L, 3, 1, 3, 50),
            NivelConfigDTO.of(20, 210000L, 3, 1, 3, 50),
            NivelConfigDTO.of(21, 231000L, 3, 1, 3, 75),
            NivelConfigDTO.of(22, 253000L, 3, 1, 3, 75),
            NivelConfigDTO.of(23, 276000L, 3, 1, 3, 75),
            NivelConfigDTO.of(24, 300000L, 3, 1, 3, 75),
            NivelConfigDTO.of(25, 325000L, 3, 1, 3, 75),
            NivelConfigDTO.of(26, 351000L, 3, 1, 3, 100),
            NivelConfigDTO.of(27, 378000L, 3, 1, 3, 100),
            NivelConfigDTO.of(28, 406000L, 3, 1, 3, 100),
            NivelConfigDTO.of(29, 435000L, 3, 1, 3, 100),
            NivelConfigDTO.of(30, 465000L, 3, 1, 3, 100),
            NivelConfigDTO.of(31, 496000L, 3, 1, 3, 120),
            NivelConfigDTO.of(32, 528000L, 3, 1, 3, 120),
            NivelConfigDTO.of(33, 561000L, 3, 1, 3, 120),
            NivelConfigDTO.of(34, 595000L, 3, 1, 3, 120),
            NivelConfigDTO.of(35, 630000L, 3, 1, 3, 120)
        );
    }

    public List<LimitadorConfigDTO> getLimitadores() {
        return List.of(
            LimitadorConfigDTO.of(0,  1,  10),
            LimitadorConfigDTO.of(2,  20, 50),
            LimitadorConfigDTO.of(21, 25, 75),
            LimitadorConfigDTO.of(26, 30, 100),
            LimitadorConfigDTO.of(31, 35, 120)
        );
    }
}
