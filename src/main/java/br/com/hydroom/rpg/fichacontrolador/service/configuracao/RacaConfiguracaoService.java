package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Raças.
 *
 * <p>Raças definem a espécie/etnia do personagem (Humano, Elfo, Anão, etc.)</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class RacaConfiguracaoService extends AbstractConfiguracaoService<Raca, ConfiguracaoRacaRepository> {

    public RacaConfiguracaoService(ConfiguracaoRacaRepository repository) {
        super(repository, "Raça");
    }

    @Override
    public List<Raca> listar(Long jogoId) {
        log.debug("Listando raças para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(Raca configuracao) {
        validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
    }

    @Override
    protected void validarAntesAtualizar(Raca configuracaoExistente, Raca configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            validateUniqueNome(configuracaoAtualizada.getNome(), configuracaoExistente.getJogo().getId());
        }
    }

    @Override
    protected void atualizarCampos(Raca existente, Raca atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }

    private void validateUniqueNome(String nome, Long jogoId) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(jogoId, nome)) {
            throw new ConflictException("Já existe uma raça com o nome '" + nome + "' neste jogo");
        }
    }
}
