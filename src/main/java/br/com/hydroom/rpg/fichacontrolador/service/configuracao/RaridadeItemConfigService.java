package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de raridade de itens.
 *
 * <p>Define os níveis de raridade disponíveis no jogo e suas propriedades.</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class RaridadeItemConfigService extends AbstractConfiguracaoService<RaridadeItemConfig, RaridadeItemConfigRepository> {

    public RaridadeItemConfigService(RaridadeItemConfigRepository repository) {
        super(repository, "RaridadeItemConfig");
    }

    @Override
    public List<RaridadeItemConfig> listar(Long jogoId) {
        log.debug("Listando raridades de item para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(RaridadeItemConfig configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException("Já existe uma raridade com o nome '" + configuracao.getNome() + "' neste jogo");
        }
    }

    @Override
    protected void validarAntesAtualizar(RaridadeItemConfig existente, RaridadeItemConfig atualizado) {
        if (atualizado.getNome() != null && !existente.getNome().equalsIgnoreCase(atualizado.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCase(existente.getJogo().getId(), atualizado.getNome())) {
                throw new ConflictException("Já existe uma raridade com o nome '" + atualizado.getNome() + "' neste jogo");
            }
        }
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        // TODO RN-T1-05: Antes de deletar, verificar se esta raridade está sendo usada em ItemConfig.
        // Quando ItemConfig for implementada (Spec 016 T3+), adicionar:
        // if (itemConfigRepository.existsByRaridadeId(id)) {
        //     long count = itemConfigRepository.countByRaridadeId(id);
        //     throw new ConflictException("Raridade usada em " + count + " itens e não pode ser removida");
        // }
        super.deletar(id);
    }

    @Override
    protected void atualizarCampos(RaridadeItemConfig existente, RaridadeItemConfig atualizado) {
        if (atualizado.getNome() != null) {
            existente.setNome(atualizado.getNome());
        }
        if (atualizado.getCor() != null) {
            existente.setCor(atualizado.getCor());
        }
        if (atualizado.getOrdemExibicao() > 0) {
            existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        }
        existente.setPodeJogadorAdicionar(atualizado.isPodeJogadorAdicionar());
        existente.setBonusAtributoMin(atualizado.getBonusAtributoMin());
        existente.setBonusAtributoMax(atualizado.getBonusAtributoMax());
        existente.setBonusDerivadoMin(atualizado.getBonusDerivadoMin());
        existente.setBonusDerivadoMax(atualizado.getBonusDerivadoMax());
        if (atualizado.getDescricao() != null) {
            existente.setDescricao(atualizado.getDescricao());
        }
    }
}
