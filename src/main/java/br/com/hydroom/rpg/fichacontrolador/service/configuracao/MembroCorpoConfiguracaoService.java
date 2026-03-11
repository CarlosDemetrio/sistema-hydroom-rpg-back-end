package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.MembroCorpoConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Membros do Corpo.
 *
 * <p>Membros do corpo representam partes do personagem para cálculo de dano localizado</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class MembroCorpoConfiguracaoService extends AbstractConfiguracaoService<MembroCorpoConfig, MembroCorpoConfigRepository> {

    public MembroCorpoConfiguracaoService(MembroCorpoConfigRepository repository) {
        super(repository, "Membro do corpo");
    }

    @Override
    public List<MembroCorpoConfig> listar(Long jogoId) {
        log.debug("Listando membros do corpo para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    public List<MembroCorpoConfig> listar(Long jogoId, String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(jogoId, nome);
        }
        return listar(jogoId);
    }

    @Override
    protected void atualizarCampos(MembroCorpoConfig existente, MembroCorpoConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setPorcentagemVida(atualizado.getPorcentagemVida());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
