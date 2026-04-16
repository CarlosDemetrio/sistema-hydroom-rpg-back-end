package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.FocoNpc;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeAtributo;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.NpcDificuldadeConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de níveis de dificuldade de NPCs.
 *
 * <p>Permite ao Mestre definir níveis pré-configurados (ex: Fácil, Médio, Difícil) com
 * valores de atributo pré-definidos para agilizar a criação de NPCs.</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class NpcDificuldadeConfiguracaoService
        extends AbstractConfiguracaoService<NpcDificuldadeConfig, NpcDificuldadeConfigRepository> {

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    public NpcDificuldadeConfiguracaoService(NpcDificuldadeConfigRepository repository) {
        super(repository, "NpcDificuldadeConfig");
    }

    @Override
    public NpcDificuldadeConfig buscarPorId(Long id) {
        return repository.findByIdWithValores(id)
                .orElseThrow(() -> new ResourceNotFoundException("NpcDificuldadeConfig", id));
    }

    @Override
    public List<NpcDificuldadeConfig> listar(Long jogoId) {
        log.debug("Listando configurações de dificuldade NPC para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    /**
     * Cria um NpcDificuldadeConfig, preenchendo {@code ordemExibicao} automaticamente
     * como {@code MAX + 1} se o valor não for informado (null ou 0).
     */
    @Override
    @Transactional
    public NpcDificuldadeConfig criar(NpcDificuldadeConfig configuracao) {
        if (configuracao.getOrdemExibicao() == null || configuracao.getOrdemExibicao() == 0) {
            configuracao.setOrdemExibicao(
                calcularProximaOrdemExibicao(configuracao.getJogo().getId(), "NpcDificuldadeConfig"));
        }
        return super.criar(configuracao);
    }

    public List<NpcDificuldadeConfig> listarPorFoco(Long jogoId, FocoNpc foco) {
        log.debug("Listando configurações de dificuldade NPC por foco={} para jogo ID: {}", foco, jogoId);
        return repository.findByJogoIdAndFocoOrderByOrdemExibicao(jogoId, foco);
    }

    @Override
    protected void validarAntesCriar(NpcDificuldadeConfig configuracao) {
        validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
    }

    @Override
    protected void validarAntesAtualizar(NpcDificuldadeConfig existente, NpcDificuldadeConfig atualizado) {
        if (atualizado.getNome() != null && !atualizado.getNome().equalsIgnoreCase(existente.getNome())) {
            validateUniqueNome(atualizado.getNome(), existente.getJogo().getId());
        }
    }

    @Override
    protected void atualizarCampos(NpcDificuldadeConfig existente, NpcDificuldadeConfig atualizado) {
        if (atualizado.getNome() != null) {
            existente.setNome(atualizado.getNome());
        }
        if (atualizado.getDescricao() != null) {
            existente.setDescricao(atualizado.getDescricao());
        }
        if (atualizado.getFoco() != null) {
            existente.setFoco(atualizado.getFoco());
        }
        if (atualizado.getOrdemExibicao() != null) {
            existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        }
        // valoresAtributo: gerenciado diretamente no controller via substituirValoresAtributo()
        // antes de chamar atualizar(), para preservar a referência gerenciada pelo Hibernate.
    }

    /**
     * Substitui a lista de valoresAtributo preservando a referência gerenciada pelo Hibernate.
     *
     * <p>Usar clear()+addAll() em vez de setValoresAtributo() evita o erro Hibernate:
     * "A collection with orphan deletion was no longer referenced by the owning entity".</p>
     *
     * @param config      entidade gerenciada com a coleção existente
     * @param novosValores nova lista já construída com npcDificuldadeConfig setado
     */
    public void substituirValoresAtributo(NpcDificuldadeConfig config, List<NpcDificuldadeAtributo> novosValores) {
        config.getValoresAtributo().clear();
        config.getValoresAtributo().addAll(novosValores);
    }

    /**
     * Carrega AtributoConfig por ID, validando existência.
     */
    public AtributoConfig buscarAtributo(Long atributoId) {
        return atributoRepository.findById(atributoId)
                .orElseThrow(() -> new ResourceNotFoundException("AtributoConfig", atributoId));
    }

    private void validateUniqueNome(String nome, Long jogoId) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(jogoId, nome)) {
            throw new ConflictException("Já existe um nível de dificuldade NPC com o nome '" + nome + "' neste jogo");
        }
    }
}
