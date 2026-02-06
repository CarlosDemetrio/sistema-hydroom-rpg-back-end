package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

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

    @Override
    protected void atualizarCampos(TipoAptidao existente, TipoAptidao atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
