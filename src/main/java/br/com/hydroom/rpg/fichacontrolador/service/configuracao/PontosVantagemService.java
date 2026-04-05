package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.PontosVantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.PontosVantagemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de pontos de vantagem por nível.
 *
 * <p>Define quantos pontos de vantagem o personagem ganha ao atingir cada nível.</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class PontosVantagemService extends AbstractConfiguracaoService<PontosVantagemConfig, PontosVantagemConfigRepository> {

    public PontosVantagemService(PontosVantagemConfigRepository repository) {
        super(repository, "Pontos de Vantagem");
    }

    @Override
    public List<PontosVantagemConfig> listar(Long jogoId) {
        log.debug("Listando pontos de vantagem para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByNivel(jogoId);
    }

    @Override
    protected void validarAntesCriar(PontosVantagemConfig configuracao) {
        if (repository.existsByJogoIdAndNivel(configuracao.getJogo().getId(), configuracao.getNivel())) {
            throw new ConflictException(
                "Já existe configuração de pontos de vantagem para o nível " + configuracao.getNivel() + " neste jogo");
        }
    }

    @Override
    protected void validarAntesAtualizar(PontosVantagemConfig existente, PontosVantagemConfig atualizado) {
        if (!existente.getNivel().equals(atualizado.getNivel())) {
            if (repository.existsByJogoIdAndNivelAndIdNot(
                    existente.getJogo().getId(), atualizado.getNivel(), existente.getId())) {
                throw new ConflictException(
                    "Já existe configuração de pontos de vantagem para o nível " + atualizado.getNivel() + " neste jogo");
            }
        }
    }

    @Override
    protected void atualizarCampos(PontosVantagemConfig existente, PontosVantagemConfig atualizado) {
        existente.setNivel(atualizado.getNivel());
        existente.setPontosGanhos(atualizado.getPontosGanhos());
    }
}
