package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import java.util.List;

/**
 * Interface base para serviços de configuração do sistema RPG.
 *
 * <p>Define operações CRUD padrão para todas as configurações do jogo.
 * Todas as configurações são POR JOGO - cada mestre pode ter suas próprias configs.</p>
 *
 * @param <T> Tipo da entidade de configuração
 * @author Ficha Controlador Team
 * @since 1.0
 */
public interface BaseConfiguracaoService<T> {

    /**
     * Lista todas as configurações de um jogo específico.
     *
     * @param jogoId ID do jogo
     * @return Lista de configurações ordenadas
     */
    List<T> listar(Long jogoId);

    /**
     * Busca uma configuração específica por ID.
     *
     * @param id ID da configuração
     * @return Configuração encontrada
     * @throws br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException se não encontrada
     */
    T buscarPorId(Long id);

    /**
     * Cria uma nova configuração.
     *
     * @param configuracao Configuração a ser criada
     * @return Configuração criada
     * @throws br.com.hydroom.rpg.fichacontrolador.exception.ConflictException se houver conflito
     */
    T criar(T configuracao);

    /**
     * Atualiza uma configuração existente.
     *
     * @param id ID da configuração a ser atualizada
     * @param configuracao Dados atualizados
     * @return Configuração atualizada
     * @throws br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException se não encontrada
     */
    T atualizar(Long id, T configuracao);

    /**
     * Deleta uma configuração (soft delete).
     *
     * @param id ID da configuração a ser deletada
     * @throws br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException se não encontrada
     */
    void deletar(Long id);
}
