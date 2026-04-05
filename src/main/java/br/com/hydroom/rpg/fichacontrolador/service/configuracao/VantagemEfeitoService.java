package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CriarVantagemEfeitoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemEfeito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.MembroCorpoConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemEfeitoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de efeitos de vantagem.
 *
 * <p>Cada vantagem pode ter múltiplos efeitos (BONUS_ATRIBUTO, BONUS_APTIDAO, etc.).
 * Este service valida que os alvos pertencem ao mesmo jogo e que os campos obrigatórios
 * estão presentes conforme o tipoEfeito.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VantagemEfeitoService {

    private final VantagemEfeitoRepository efeitoRepository;
    private final VantagemConfigRepository vantagemRepository;
    private final ConfiguracaoAtributoRepository atributoRepository;
    private final ConfiguracaoAptidaoRepository aptidaoRepository;
    private final BonusConfigRepository bonusRepository;
    private final MembroCorpoConfigRepository membroRepository;

    /**
     * Lista todos os efeitos de uma vantagem.
     */
    public List<VantagemEfeito> listarPorVantagem(Long vantagemConfigId) {
        // Valida que a vantagem existe
        if (!vantagemRepository.existsById(vantagemConfigId)) {
            throw new ResourceNotFoundException("VantagemConfig", vantagemConfigId);
        }
        return efeitoRepository.findByVantagemConfigId(vantagemConfigId);
    }

    /**
     * Cria um novo efeito para uma vantagem.
     *
     * <p>Valida:</p>
     * <ul>
     *   <li>Que a vantagem pertence ao jogoId informado</li>
     *   <li>Que o alvo (atributo/aptidão/bônus/membro) pertence ao mesmo jogo</li>
     *   <li>Que tipoEfeito tem os campos corretos preenchidos</li>
     * </ul>
     */
    @Transactional
    public VantagemEfeito criar(Long vantagemConfigId, Long jogoId, CriarVantagemEfeitoRequest request) {
        VantagemConfig vantagem = vantagemRepository.findById(vantagemConfigId)
            .orElseThrow(() -> new ResourceNotFoundException("VantagemConfig", vantagemConfigId));

        if (!vantagem.getJogo().getId().equals(jogoId)) {
            throw new ValidationException("A vantagem não pertence ao jogo informado");
        }

        VantagemEfeito efeito = VantagemEfeito.builder()
            .vantagemConfig(vantagem)
            .tipoEfeito(request.tipoEfeito())
            .valorFixo(request.valorFixo())
            .valorPorNivel(request.valorPorNivel())
            .formula(request.formula())
            .descricaoEfeito(request.descricaoEfeito())
            .build();

        resolverAlvo(efeito, request, jogoId);
        validarCamposObrigatorios(efeito);

        log.debug("Criando efeito {} para vantagem ID: {}", request.tipoEfeito(), vantagemConfigId);
        return efeitoRepository.save(efeito);
    }

    /**
     * Deleta um efeito de vantagem.
     */
    @Transactional
    public void deletar(Long efeitoId, Long jogoId) {
        VantagemEfeito efeito = efeitoRepository.findById(efeitoId)
            .orElseThrow(() -> new ResourceNotFoundException("VantagemEfeito", efeitoId));

        if (!efeito.getVantagemConfig().getJogo().getId().equals(jogoId)) {
            throw new ValidationException("O efeito não pertence ao jogo informado");
        }

        efeitoRepository.delete(efeito);
        log.debug("Efeito ID {} deletado", efeitoId);
    }

    // ===== HELPERS PRIVADOS =====

    /**
     * Resolve a FK de alvo conforme o tipoEfeito, validando que pertence ao mesmo jogo.
     */
    private void resolverAlvo(VantagemEfeito efeito, CriarVantagemEfeitoRequest request, Long jogoId) {
        switch (request.tipoEfeito()) {
            case BONUS_ATRIBUTO -> {
                if (request.atributoAlvoId() == null) {
                    throw new ValidationException("atributoAlvoId é obrigatório para BONUS_ATRIBUTO");
                }
                AtributoConfig atributo = atributoRepository.findById(request.atributoAlvoId())
                    .orElseThrow(() -> new ResourceNotFoundException("AtributoConfig", request.atributoAlvoId()));
                if (!atributo.getJogo().getId().equals(jogoId)) {
                    throw new ValidationException("AtributoConfig não pertence ao jogo informado");
                }
                efeito.setAtributoAlvo(atributo);
            }
            case BONUS_APTIDAO -> {
                if (request.aptidaoAlvoId() == null) {
                    throw new ValidationException("aptidaoAlvoId é obrigatório para BONUS_APTIDAO");
                }
                AptidaoConfig aptidao = aptidaoRepository.findById(request.aptidaoAlvoId())
                    .orElseThrow(() -> new ResourceNotFoundException("AptidaoConfig", request.aptidaoAlvoId()));
                if (!aptidao.getJogo().getId().equals(jogoId)) {
                    throw new ValidationException("AptidaoConfig não pertence ao jogo informado");
                }
                efeito.setAptidaoAlvo(aptidao);
            }
            case BONUS_DERIVADO -> {
                if (request.bonusAlvoId() == null) {
                    throw new ValidationException("bonusAlvoId é obrigatório para BONUS_DERIVADO");
                }
                BonusConfig bonus = bonusRepository.findById(request.bonusAlvoId())
                    .orElseThrow(() -> new ResourceNotFoundException("BonusConfig", request.bonusAlvoId()));
                if (!bonus.getJogo().getId().equals(jogoId)) {
                    throw new ValidationException("BonusConfig não pertence ao jogo informado");
                }
                efeito.setBonusAlvo(bonus);
            }
            case BONUS_VIDA_MEMBRO -> {
                if (request.membroAlvoId() == null) {
                    throw new ValidationException("membroAlvoId é obrigatório para BONUS_VIDA_MEMBRO");
                }
                MembroCorpoConfig membro = membroRepository.findById(request.membroAlvoId())
                    .orElseThrow(() -> new ResourceNotFoundException("MembroCorpoConfig", request.membroAlvoId()));
                if (!membro.getJogo().getId().equals(jogoId)) {
                    throw new ValidationException("MembroCorpoConfig não pertence ao jogo informado");
                }
                efeito.setMembroAlvo(membro);
            }
            case BONUS_VIDA, BONUS_ESSENCIA, DADO_UP, FORMULA_CUSTOMIZADA -> {
                // Sem FK de alvo necessária — a validação de campos numéricos é feita em validarCamposObrigatorios
            }
        }
    }

    /**
     * Valida campos obrigatórios conforme o tipoEfeito.
     */
    private void validarCamposObrigatorios(VantagemEfeito efeito) {
        switch (efeito.getTipoEfeito()) {
            case FORMULA_CUSTOMIZADA -> {
                if (efeito.getFormula() == null || efeito.getFormula().isBlank()) {
                    throw new ValidationException("formula é obrigatória para FORMULA_CUSTOMIZADA");
                }
            }
            case DADO_UP -> {
                // DADO_UP não usa valor numérico — incremento implícito de +1 face
            }
            default -> {
                if (efeito.getValorFixo() == null && efeito.getValorPorNivel() == null) {
                    throw new ValidationException(
                        "Ao menos valorFixo ou valorPorNivel deve ser informado para o tipo " + efeito.getTipoEfeito()
                    );
                }
            }
        }
    }
}
