package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoItemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de itens.
 *
 * <p>Valida unicidade de nome por jogo, que raridade e tipo pertencem ao mesmo jogo,
 * e gerencia o CRUD completo de ItemConfig.</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class ItemConfigService extends AbstractConfiguracaoService<ItemConfig, ItemConfigRepository> {

    private final RaridadeItemConfigRepository raridadeRepository;
    private final TipoItemConfigRepository tipoRepository;

    public ItemConfigService(
            ItemConfigRepository repository,
            RaridadeItemConfigRepository raridadeRepository,
            TipoItemConfigRepository tipoRepository) {
        super(repository, "ItemConfig");
        this.raridadeRepository = raridadeRepository;
        this.tipoRepository = tipoRepository;
    }

    @Override
    public List<ItemConfig> listar(Long jogoId) {
        log.debug("Listando itens para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    /**
     * Cria um ItemConfig, preenchendo {@code ordemExibicao} automaticamente
     * como {@code MAX + 1} se o valor não for informado (null ou 0).
     */
    @Override
    @Transactional
    public ItemConfig criar(ItemConfig configuracao) {
        if (configuracao.getOrdemExibicao() == 0) {
            configuracao.setOrdemExibicao(
                calcularProximaOrdemExibicao(configuracao.getJogo().getId(), "ItemConfig"));
        }
        return super.criar(configuracao);
    }

    /**
     * Busca ItemConfig por ID carregando efeitos e requisitos (evita LazyInitializationException).
     *
     * <p>Usa JOIN FETCH para efeitos e inicializa requisitos separadamente
     * para evitar MultipleBagFetchException.</p>
     */
    @Override
    public ItemConfig buscarPorId(Long id) {
        log.debug("Buscando ItemConfig ID: {}", id);
        ItemConfig item = repository.findByIdWithEfeitos(id)
            .orElseThrow(() -> new ResourceNotFoundException("ItemConfig", id));
        Hibernate.initialize(item.getRequisitos());
        return item;
    }

    @Override
    protected void validarAntesCriar(ItemConfig configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException("Já existe um item com o nome '" + configuracao.getNome() + "' neste jogo");
        }
        validarRaridadeEhDoMesmoJogo(configuracao);
        validarTipoEhDoMesmoJogo(configuracao);
    }

    @Override
    protected void validarAntesAtualizar(ItemConfig existente, ItemConfig atualizado) {
        if (atualizado.getNome() != null && !existente.getNome().equalsIgnoreCase(atualizado.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCaseAndIdNot(
                    existente.getJogo().getId(), atualizado.getNome(), existente.getId())) {
                throw new ConflictException("Já existe um item com o nome '" + atualizado.getNome() + "' neste jogo");
            }
        }
        if (atualizado.getRaridade() != null) {
            validarRaridadeEhDoMesmoJogo(atualizado.getRaridade().getId(), existente.getJogo().getId());
        }
        if (atualizado.getTipo() != null) {
            validarTipoEhDoMesmoJogo(atualizado.getTipo().getId(), existente.getJogo().getId());
        }
    }

    @Override
    protected void atualizarCampos(ItemConfig existente, ItemConfig atualizado) {
        if (atualizado.getNome() != null) {
            existente.setNome(atualizado.getNome());
        }
        if (atualizado.getRaridade() != null) {
            existente.setRaridade(atualizado.getRaridade());
        }
        if (atualizado.getTipo() != null) {
            existente.setTipo(atualizado.getTipo());
        }
        if (atualizado.getPeso() != null) {
            existente.setPeso(atualizado.getPeso());
        }
        if (atualizado.getValor() != null) {
            existente.setValor(atualizado.getValor());
        }
        if (atualizado.getDuracaoPadrao() != null) {
            existente.setDuracaoPadrao(atualizado.getDuracaoPadrao());
        }
        if (atualizado.getNivelMinimo() > 0) {
            existente.setNivelMinimo(atualizado.getNivelMinimo());
        }
        if (atualizado.getPropriedades() != null) {
            existente.setPropriedades(atualizado.getPropriedades());
        }
        if (atualizado.getDescricao() != null) {
            existente.setDescricao(atualizado.getDescricao());
        }
        if (atualizado.getOrdemExibicao() > 0) {
            existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        }
    }

    /**
     * Listagem paginada com filtros opcionais por nome, raridade e categoria.
     *
     * <p>O parâmetro {@code nomeQuery} é pré-processado aqui para evitar
     * {@code LOWER(null)} no PostgreSQL ({@code function lower(bytea) does not exist}).
     * Se nulo ou vazio, passa {@code null} ao repositório (sem filtro de nome).
     * Se informado, converte para {@code "%termo%"} em lowercase antes de passar.</p>
     */
    public Page<ItemConfig> listarComFiltros(
            Long jogoId, String nomeQuery, Long raridadeId, CategoriaItem categoriaItem, Pageable pageable) {
        log.debug("Listando itens do jogo {} com filtros: nome={}, raridade={}, categoria={}",
            jogoId, nomeQuery, raridadeId, categoriaItem);
        String nomeLike = (nomeQuery != null && !nomeQuery.isBlank())
            ? "%" + nomeQuery.toLowerCase() + "%"
            : null;
        return repository.findByJogoIdWithFilters(jogoId, nomeLike, raridadeId, categoriaItem, pageable);
    }

    /**
     * Busca RaridadeItemConfig por ID ou lança ResourceNotFoundException.
     */
    public RaridadeItemConfig buscarRaridade(Long raridadeId) {
        return raridadeRepository.findById(raridadeId)
            .orElseThrow(() -> new ResourceNotFoundException("RaridadeItemConfig", raridadeId));
    }

    /**
     * Busca TipoItemConfig por ID ou lança ResourceNotFoundException.
     */
    public TipoItemConfig buscarTipo(Long tipoId) {
        return tipoRepository.findById(tipoId)
            .orElseThrow(() -> new ResourceNotFoundException("TipoItemConfig", tipoId));
    }

    // ===== HELPERS PRIVADOS =====

    private void validarRaridadeEhDoMesmoJogo(ItemConfig configuracao) {
        if (configuracao.getRaridade() != null) {
            validarRaridadeEhDoMesmoJogo(configuracao.getRaridade().getId(), configuracao.getJogo().getId());
        }
    }

    private void validarRaridadeEhDoMesmoJogo(Long raridadeId, Long jogoId) {
        RaridadeItemConfig raridade = raridadeRepository.findById(raridadeId)
            .orElseThrow(() -> new ResourceNotFoundException("RaridadeItemConfig", raridadeId));
        if (!raridade.getJogo().getId().equals(jogoId)) {
            throw new ValidationException("A raridade informada não pertence ao jogo deste item");
        }
    }

    private void validarTipoEhDoMesmoJogo(ItemConfig configuracao) {
        if (configuracao.getTipo() != null) {
            validarTipoEhDoMesmoJogo(configuracao.getTipo().getId(), configuracao.getJogo().getId());
        }
    }

    private void validarTipoEhDoMesmoJogo(Long tipoId, Long jogoId) {
        TipoItemConfig tipo = tipoRepository.findById(tipoId)
            .orElseThrow(() -> new ResourceNotFoundException("TipoItemConfig", tipoId));
        if (!tipo.getJogo().getId().equals(jogoId)) {
            throw new ValidationException("O tipo de item informado não pertence ao jogo deste item");
        }
    }
}
