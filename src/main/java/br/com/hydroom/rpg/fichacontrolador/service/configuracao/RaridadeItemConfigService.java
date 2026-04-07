package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String COR_REGEX = "^#[0-9A-Fa-f]{6}$";

    @Autowired
    private ItemConfigRepository itemConfigRepository;

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
        // RAR-01: validar formato hexadecimal da cor
        if (configuracao.getCor() != null && !configuracao.getCor().matches(COR_REGEX)) {
            throw new ValidationException("Cor deve ser hexadecimal no formato #RRGGBB (ex: #9d9d9d). Valor inválido: '"
                    + configuracao.getCor() + "'");
        }
        // RAR-02: validar ordemExibicao único por jogo
        if (repository.existsByJogoIdAndOrdemExibicao(configuracao.getJogo().getId(), configuracao.getOrdemExibicao())) {
            throw new ConflictException("Já existe uma raridade com ordemExibicao "
                    + configuracao.getOrdemExibicao() + " neste jogo");
        }
        // RAR-05: validar ranges de bônus
        if (configuracao.getBonusAtributoMin() != null && configuracao.getBonusAtributoMax() != null
                && configuracao.getBonusAtributoMin() > configuracao.getBonusAtributoMax()) {
            throw new ValidationException("bonusAtributoMin não pode ser maior que bonusAtributoMax");
        }
        if (configuracao.getBonusDerivadoMin() != null && configuracao.getBonusDerivadoMax() != null
                && configuracao.getBonusDerivadoMin() > configuracao.getBonusDerivadoMax()) {
            throw new ValidationException("bonusDerivadoMin não pode ser maior que bonusDerivadoMax");
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
        // RAR-03: verificar se raridade está sendo usada em ItemConfig
        if (itemConfigRepository.existsByRaridadeId(id)) {
            throw new ConflictException("Raridade está em uso em itens do catálogo e não pode ser removida");
        }
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
