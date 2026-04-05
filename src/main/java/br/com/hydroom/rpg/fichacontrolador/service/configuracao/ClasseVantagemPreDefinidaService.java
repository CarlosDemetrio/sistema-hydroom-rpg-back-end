package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseVantagemPreDefinidaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseVantagemPreDefinida;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ClasseVantagemPreDefinidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de vantagens pré-definidas por classe/nível.
 *
 * <p>RN-015-06: VantagemConfig deve pertencer ao mesmo jogo da ClassePersonagem.</p>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClasseVantagemPreDefinidaService {

    private final ClasseVantagemPreDefinidaRepository repository;
    private final ConfiguracaoClasseRepository classeRepository;
    private final VantagemConfigRepository vantagemConfigRepository;

    public List<ClasseVantagemPreDefinida> listarPorClasse(Long classeId) {
        return repository.findByClassePersonagemIdOrderByNivel(classeId);
    }

    @Transactional
    public ClasseVantagemPreDefinida criar(Long classeId, ClasseVantagemPreDefinidaRequest request) {
        ClassePersonagem classe = classeRepository.findById(classeId)
            .orElseThrow(() -> new ResourceNotFoundException("ClassePersonagem", classeId));

        VantagemConfig vantagem = vantagemConfigRepository.findById(request.vantagemConfigId())
            .orElseThrow(() -> new ResourceNotFoundException("VantagemConfig", request.vantagemConfigId()));

        if (!vantagem.getJogo().getId().equals(classe.getJogo().getId())) {
            throw new ValidationException("VantagemConfig deve pertencer ao mesmo jogo da classe");
        }

        if (repository.existsByClassePersonagemIdAndNivelAndVantagemConfigId(
                classeId, request.nivel(), request.vantagemConfigId())) {
            throw new ConflictException(
                "Vantagem '" + vantagem.getNome() + "' já está pré-definida para o nível " + request.nivel());
        }

        ClasseVantagemPreDefinida predefinida = ClasseVantagemPreDefinida.builder()
            .classePersonagem(classe)
            .nivel(request.nivel())
            .vantagemConfig(vantagem)
            .build();

        return repository.save(predefinida);
    }

    @Transactional
    public void deletar(Long classeId, Long predefinidaId) {
        ClasseVantagemPreDefinida existente = repository.findById(predefinidaId)
            .orElseThrow(() -> new ResourceNotFoundException("ClasseVantagemPreDefinida", predefinidaId));

        if (!existente.getClassePersonagem().getId().equals(classeId)) {
            throw new ValidationException("ClasseVantagemPreDefinida não pertence à classe informada.");
        }

        existente.delete();
        repository.save(existente);
    }
}
