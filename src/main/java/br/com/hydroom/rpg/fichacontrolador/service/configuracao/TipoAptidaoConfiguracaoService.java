package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoAptidaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Tipos de Aptidão.
 *
 * <p>Tipos de Aptidão categorizam as aptidões do sistema (Combate, Magia, Social, etc.)</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class TipoAptidaoConfiguracaoService extends AbstractConfiguracaoService<TipoAptidao, TipoAptidaoRepository> {

    public TipoAptidaoConfiguracaoService(TipoAptidaoRepository repository) {
        super(repository, "Tipo de aptidão");
    }

    @Override
    public List<TipoAptidao> listar(Long jogoId) {
        log.debug("Listando tipos de aptidão para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    public List<TipoAptidao> listar(Long jogoId, String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(jogoId, nome);
        }
        return listar(jogoId);
    }

    @Override
    protected void validarAntesCriar(TipoAptidao configuracao) {
        validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
    }

    @Override
    protected void validarAntesAtualizar(TipoAptidao configuracaoExistente, TipoAptidao configuracaoAtualizada) {
        // Validar nome único apenas se mudou
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            validateUniqueNome(configuracaoAtualizada.getNome(), configuracaoExistente.getJogo().getId());
        }
    }

    @Override
    protected void atualizarCampos(TipoAptidao existente, TipoAptidao atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }

    /**
     * Valida se já existe um tipo de aptidão com o mesmo nome no jogo.
     *
     * @param nome Nome do tipo de aptidão
     * @param jogoId ID do jogo
     * @throws ConflictException se nome já existe
     */
    private void validateUniqueNome(String nome, Long jogoId) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(jogoId, nome)) {
            throw new ConflictException("Já existe um tipo de aptidão com o nome '" + nome + "' neste jogo");
        }
    }
}
