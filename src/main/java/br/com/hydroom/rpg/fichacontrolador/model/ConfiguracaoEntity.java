package br.com.hydroom.rpg.fichacontrolador.model;

/**
 * Interface marcadora para entidades de configuração do sistema RPG.
 *
 * <p>Todas as configurações do jogo devem implementar esta interface
 * para garantir acesso aos métodos básicos necessários.</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
public interface ConfiguracaoEntity {

    /**
     * Obtém o ID da configuração.
     *
     * @return ID da configuração
     */
    Long getId();

    /**
     * Obtém o jogo ao qual esta configuração pertence.
     *
     * @return Jogo da configuração
     */
    Jogo getJogo();
}
