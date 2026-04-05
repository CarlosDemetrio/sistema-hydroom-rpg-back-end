package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePontosConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ClassePontosConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de pontos extras por nível concedidos por classe.
 *
 * <p>Sub-recurso de ClassePersonagem. pontosAptidao ausente por decisão PO 2026-04-04:
 * aptidões são completamente independentes de classe/raça.</p>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClassePontosConfigService {

    private final ClassePontosConfigRepository repository;
    private final ConfiguracaoClasseRepository classeRepository;

    public List<ClassePontosConfig> listarPorClasse(Long classeId) {
        return repository.findByClassePersonagemIdOrderByNivel(classeId);
    }

    @Transactional
    public ClassePontosConfig criar(Long classeId, ClassePontosConfig pontosConfig) {
        ClassePersonagem classe = classeRepository.findById(classeId)
            .orElseThrow(() -> new ResourceNotFoundException("ClassePersonagem", classeId));

        if (repository.existsByClassePersonagemIdAndNivel(classeId, pontosConfig.getNivel())) {
            throw new ConflictException(
                "Já existe configuração de pontos para o nível " + pontosConfig.getNivel()
                + " na classe " + classe.getNome());
        }

        pontosConfig.setClassePersonagem(classe);
        return repository.save(pontosConfig);
    }

    @Transactional
    public ClassePontosConfig atualizar(Long classeId, Long pontosConfigId, ClassePontosConfig atualizado) {
        ClassePontosConfig existente = repository.findById(pontosConfigId)
            .orElseThrow(() -> new ResourceNotFoundException("ClassePontosConfig", pontosConfigId));

        if (!existente.getClassePersonagem().getId().equals(classeId)) {
            throw new ValidationException("ClassePontosConfig não pertence à classe informada.");
        }

        if (!existente.getNivel().equals(atualizado.getNivel())
                && repository.existsByClassePersonagemIdAndNivel(classeId, atualizado.getNivel())) {
            throw new ConflictException(
                "Já existe configuração de pontos para o nível " + atualizado.getNivel());
        }

        existente.setNivel(atualizado.getNivel());
        existente.setPontosAtributo(atualizado.getPontosAtributo());
        existente.setPontosVantagem(atualizado.getPontosVantagem());
        return repository.save(existente);
    }

    @Transactional
    public void deletar(Long classeId, Long pontosConfigId) {
        ClassePontosConfig existente = repository.findById(pontosConfigId)
            .orElseThrow(() -> new ResourceNotFoundException("ClassePontosConfig", pontosConfigId));

        if (!existente.getClassePersonagem().getId().equals(classeId)) {
            throw new ValidationException("ClassePontosConfig não pertence à classe informada.");
        }

        existente.delete();
        repository.save(existente);
    }
}
