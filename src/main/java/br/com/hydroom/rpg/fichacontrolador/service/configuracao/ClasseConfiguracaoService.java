package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Classes de Personagem.
 *
 * <p>Classes definem o arquétipo do personagem (Guerreiro, Mago, Ladino, etc.)</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class ClasseConfiguracaoService extends AbstractConfiguracaoService<ClassePersonagem, ConfiguracaoClasseRepository> {

    public ClasseConfiguracaoService(ConfiguracaoClasseRepository repository) {
        super(repository, "Classe");
    }

    @Override
    public List<ClassePersonagem> listar(Long jogoId) {
        log.debug("Listando classes para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(ClassePersonagem configuracao) {
        validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
    }

    @Override
    protected void validarAntesAtualizar(ClassePersonagem configuracaoExistente, ClassePersonagem configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            validateUniqueNome(configuracaoAtualizada.getNome(), configuracaoExistente.getJogo().getId());
        }
    }

    @Override
    protected void atualizarCampos(ClassePersonagem existente, ClassePersonagem atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }

    private void validateUniqueNome(String nome, Long jogoId) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(jogoId, nome)) {
            throw new ConflictException("Já existe uma classe com o nome '" + nome + "' neste jogo");
        }
    }
}
