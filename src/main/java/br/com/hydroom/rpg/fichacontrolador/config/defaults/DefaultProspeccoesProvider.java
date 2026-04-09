package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ProspeccaoConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultProspeccoesProvider {

    public List<ProspeccaoConfigDTO> get() {
        return List.of(
            ProspeccaoConfigDTO.of("d3",  3,  "Dado de 3 faces, incerteza minima, usado em situacoes triviais ou de baixissimo risco",   1),
            ProspeccaoConfigDTO.of("d4",  4,  "Dado de 4 faces, pequena variacao, para situacoes simples e controladas",                  2),
            ProspeccaoConfigDTO.of("d6",  6,  "Dado de 6 faces, o dado padrao do sistema, para situacoes cotidianas e moderadas",         3),
            ProspeccaoConfigDTO.of("d8",  8,  "Dado de 8 faces, dificuldade moderada, para desafios com risco real",                      4),
            ProspeccaoConfigDTO.of("d10", 10, "Dado de 10 faces, alta dificuldade, para situacoes arriscadas ou complexas",               5),
            ProspeccaoConfigDTO.of("d12", 12, "Dado de 12 faces, extremo ou raro, reservado para proezas epicas e eventos criticos",      6),
            ProspeccaoConfigDTO.of("d20", 20, "Dado de 20 faces, extremo ou raro, reservado para proezas epicas e eventos criticos",      7)
        );
    }
}
