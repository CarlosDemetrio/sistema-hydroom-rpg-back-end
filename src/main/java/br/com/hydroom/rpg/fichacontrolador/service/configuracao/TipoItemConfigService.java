package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoItemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de tipo de itens.
 *
 * <p>Define as categorias e subcategorias de itens disponíveis no jogo.</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class TipoItemConfigService extends AbstractConfiguracaoService<TipoItemConfig, TipoItemConfigRepository> {

    @Autowired
    private ItemConfigRepository itemConfigRepository;

    public TipoItemConfigService(TipoItemConfigRepository repository) {
        super(repository, "TipoItemConfig");
    }

    @Override
    public List<TipoItemConfig> listar(Long jogoId) {
        log.debug("Listando tipos de item para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(TipoItemConfig configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException("Já existe um tipo de item com o nome '" + configuracao.getNome() + "' neste jogo");
        }
    }

    @Override
    protected void validarAntesAtualizar(TipoItemConfig existente, TipoItemConfig atualizado) {
        if (atualizado.getNome() != null && !existente.getNome().equalsIgnoreCase(atualizado.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCase(existente.getJogo().getId(), atualizado.getNome())) {
                throw new ConflictException("Já existe um tipo de item com o nome '" + atualizado.getNome() + "' neste jogo");
            }
        }
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        // TIPO-03: verificar se tipo está sendo usado em ItemConfig
        if (itemConfigRepository.existsByTipoId(id)) {
            throw new ConflictException("Tipo de item está em uso em itens do catálogo e não pode ser removido");
        }
        super.deletar(id);
    }

    @Override
    protected void atualizarCampos(TipoItemConfig existente, TipoItemConfig atualizado) {
        if (atualizado.getNome() != null) {
            existente.setNome(atualizado.getNome());
        }
        if (atualizado.getCategoria() != null) {
            existente.setCategoria(atualizado.getCategoria());
        }
        existente.setSubcategoria(atualizado.getSubcategoria());
        existente.setRequerDuasMaos(atualizado.isRequerDuasMaos());
        if (atualizado.getOrdemExibicao() > 0) {
            existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        }
        if (atualizado.getDescricao() != null) {
            existente.setDescricao(atualizado.getDescricao());
        }
    }
}
