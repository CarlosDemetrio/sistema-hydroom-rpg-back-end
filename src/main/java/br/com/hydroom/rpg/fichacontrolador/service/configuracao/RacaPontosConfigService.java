package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.RacaPontosConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RacaPontosConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de pontos extras por nível concedidos por raça.
 *
 * <p>Sub-recurso de Raca. pontosAptidao ausente por decisão PO 2026-04-04:
 * aptidões são completamente independentes de classe/raça.</p>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RacaPontosConfigService {

    private final RacaPontosConfigRepository repository;
    private final ConfiguracaoRacaRepository racaRepository;

    public List<RacaPontosConfig> listarPorRaca(Long racaId) {
        return repository.findByRacaIdOrderByNivel(racaId);
    }

    @Transactional
    public RacaPontosConfig criar(Long racaId, RacaPontosConfig pontosConfig) {
        Raca raca = racaRepository.findById(racaId)
            .orElseThrow(() -> new ResourceNotFoundException("Raca", racaId));

        if (repository.existsByRacaIdAndNivel(racaId, pontosConfig.getNivel())) {
            throw new ConflictException(
                "Já existe configuração de pontos para o nível " + pontosConfig.getNivel()
                + " na raça " + raca.getNome());
        }

        pontosConfig.setRaca(raca);
        return repository.save(pontosConfig);
    }

    @Transactional
    public RacaPontosConfig atualizar(Long racaId, Long pontosConfigId, RacaPontosConfig atualizado) {
        RacaPontosConfig existente = repository.findById(pontosConfigId)
            .orElseThrow(() -> new ResourceNotFoundException("RacaPontosConfig", pontosConfigId));

        if (!existente.getRaca().getId().equals(racaId)) {
            throw new ValidationException("RacaPontosConfig não pertence à raça informada.");
        }

        if (!existente.getNivel().equals(atualizado.getNivel())
                && repository.existsByRacaIdAndNivel(racaId, atualizado.getNivel())) {
            throw new ConflictException(
                "Já existe configuração de pontos para o nível " + atualizado.getNivel());
        }

        existente.setNivel(atualizado.getNivel());
        existente.setPontosAtributo(atualizado.getPontosAtributo());
        existente.setPontosVantagem(atualizado.getPontosVantagem());
        return repository.save(existente);
    }

    @Transactional
    public void deletar(Long racaId, Long pontosConfigId) {
        RacaPontosConfig existente = repository.findById(pontosConfigId)
            .orElseThrow(() -> new ResourceNotFoundException("RacaPontosConfig", pontosConfigId));

        if (!existente.getRaca().getId().equals(racaId)) {
            throw new ValidationException("RacaPontosConfig não pertence à raça informada.");
        }

        existente.delete();
        repository.save(existente);
    }
}
