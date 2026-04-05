package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RacaVantagemPreDefinidaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.RacaVantagemPreDefinida;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RacaVantagemPreDefinidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de vantagens pré-definidas por raça/nível.
 *
 * <p>RN-015-06: VantagemConfig deve pertencer ao mesmo jogo da Raca.</p>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RacaVantagemPreDefinidaService {

    private final RacaVantagemPreDefinidaRepository repository;
    private final ConfiguracaoRacaRepository racaRepository;
    private final VantagemConfigRepository vantagemConfigRepository;

    public List<RacaVantagemPreDefinida> listarPorRaca(Long racaId) {
        return repository.findByRacaIdOrderByNivel(racaId);
    }

    @Transactional
    public RacaVantagemPreDefinida criar(Long racaId, RacaVantagemPreDefinidaRequest request) {
        Raca raca = racaRepository.findById(racaId)
            .orElseThrow(() -> new ResourceNotFoundException("Raca", racaId));

        VantagemConfig vantagem = vantagemConfigRepository.findById(request.vantagemConfigId())
            .orElseThrow(() -> new ResourceNotFoundException("VantagemConfig", request.vantagemConfigId()));

        if (!vantagem.getJogo().getId().equals(raca.getJogo().getId())) {
            throw new ValidationException("VantagemConfig deve pertencer ao mesmo jogo da raça");
        }

        if (repository.existsByRacaIdAndNivelAndVantagemConfigId(
                racaId, request.nivel(), request.vantagemConfigId())) {
            throw new ConflictException(
                "Vantagem '" + vantagem.getNome() + "' já está pré-definida para o nível " + request.nivel());
        }

        RacaVantagemPreDefinida predefinida = RacaVantagemPreDefinida.builder()
            .raca(raca)
            .nivel(request.nivel())
            .vantagemConfig(vantagem)
            .build();

        return repository.save(predefinida);
    }

    @Transactional
    public void deletar(Long racaId, Long predefinidaId) {
        RacaVantagemPreDefinida existente = repository.findById(predefinidaId)
            .orElseThrow(() -> new ResourceNotFoundException("RacaVantagemPreDefinida", predefinidaId));

        if (!existente.getRaca().getId().equals(racaId)) {
            throw new ValidationException("RacaVantagemPreDefinida não pertence à raça informada.");
        }

        existente.delete();
        repository.save(existente);
    }
}
