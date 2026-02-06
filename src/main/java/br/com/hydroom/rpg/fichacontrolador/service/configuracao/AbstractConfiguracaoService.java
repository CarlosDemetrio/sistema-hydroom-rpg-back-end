package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.BaseEntity;
import br.com.hydroom.rpg.fichacontrolador.model.ConfiguracaoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe abstrata base para serviços de configuração.
 *
 * <p>Implementa a lógica comum de CRUD para todas as configurações do sistema.
 * Services específicas devem estender esta classe e implementar validações customizadas.</p>
 *
 * @param <T> Tipo da entidade de configuração
 * @param <R> Tipo do repositório
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Slf4j
@Transactional(readOnly = true)
public abstract class AbstractConfiguracaoService<T extends BaseEntity & ConfiguracaoEntity, R extends JpaRepository<T, Long>>
        implements BaseConfiguracaoService<T> {

    protected final R repository;
    protected final String entityName;

    protected AbstractConfiguracaoService(R repository, String entityName) {
        this.repository = repository;
        this.entityName = entityName;
    }

    @Override
    public T buscarPorId(Long id) {
        log.debug("Buscando {} ID: {}", entityName, id);
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    @Override
    @Transactional
    public T criar(T configuracao) {
        log.info("Criando {} '{}'", entityName, getEntityIdentifier(configuracao));

        validarAntesCriar(configuracao);
        configuracao.restore();

        T saved = repository.save(configuracao);
        log.info("{} criado com sucesso: ID {}", entityName, saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public T atualizar(Long id, T configuracaoAtualizada) {
        log.info("Atualizando {} ID: {}", entityName, id);

        T configuracao = buscarPorId(id);
        validarAntesAtualizar(configuracao, configuracaoAtualizada);
        atualizarCampos(configuracao, configuracaoAtualizada);

        T saved = repository.save(configuracao);
        log.info("{} atualizado com sucesso: ID {}", entityName, saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando {} ID: {}", entityName, id);

        T configuracao = buscarPorId(id);
        configuracao.delete();
        repository.save(configuracao);

        log.info("{} deletado com sucesso: ID {}", entityName, id);
    }

    /**
     * Obtém identificador da entidade para logs.
     * Subclasses podem sobrescrever para retornar informação mais específica.
     *
     * @param configuracao Configuração
     * @return String identificadora
     */
    protected String getEntityIdentifier(T configuracao) {
        return configuracao.getId() != null ? configuracao.getId().toString() : "nova";
    }

    /**
     * Valida a configuração antes de criar.
     * Subclasses devem sobrescrever para adicionar validações específicas.
     *
     * @param configuracao Configuração a ser validada
     * @throws ConflictException se houver conflito
     */
    protected void validarAntesCriar(T configuracao) {
        // Default: sem validação adicional
    }

    /**
     * Valida a configuração antes de atualizar.
     * Subclasses devem sobrescrever para adicionar validações específicas.
     *
     * @param configuracaoExistente Configuração existente no banco
     * @param configuracaoAtualizada Configuração com novos dados
     * @throws ConflictException se houver conflito
     */
    protected void validarAntesAtualizar(T configuracaoExistente, T configuracaoAtualizada) {
        // Default: sem validação adicional
    }

    /**
     * Atualiza os campos da entidade existente com os valores da entidade atualizada.
     * Subclasses DEVEM implementar este método.
     *
     * @param configuracaoExistente Configuração existente no banco
     * @param configuracaoAtualizada Configuração com novos dados
     */
    protected abstract void atualizarCampos(T configuracaoExistente, T configuracaoAtualizada);
}
