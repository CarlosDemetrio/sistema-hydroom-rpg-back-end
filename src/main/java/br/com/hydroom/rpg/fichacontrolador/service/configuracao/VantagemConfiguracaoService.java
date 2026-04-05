package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.FormulaValidationResult;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemPreRequisitoRepository;
import br.com.hydroom.rpg.fichacontrolador.service.FormulaEvaluatorService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.SiglaValidationService.TipoSigla;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service para gerenciamento de configurações de Vantagens.
 *
 * <p>Vantagens são habilidades especiais que personagens podem adquirir</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class VantagemConfiguracaoService extends AbstractConfiguracaoService<VantagemConfig, VantagemConfigRepository> {

    @Autowired
    private SiglaValidationService siglaValidationService;

    @Autowired
    private FormulaEvaluatorService formulaEvaluatorService;

    @Autowired
    private VantagemPreRequisitoRepository prerequisitoRepository;

    public VantagemConfiguracaoService(VantagemConfigRepository repository) {
        super(repository, "Vantagem");
    }

    @Override
    public VantagemConfig buscarPorId(Long id) {
        return repository.findByIdWithPreRequisitos(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vantagem", id));
    }

    @Override
    public List<VantagemConfig> listar(Long jogoId) {
        log.debug("Listando vantagens para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    public List<VantagemConfig> listar(Long jogoId, String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(jogoId, nome);
        }
        return listar(jogoId);
    }

    @Override
    protected void validarAntesCriar(VantagemConfig configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException("Já existe uma vantagem com o nome '" + configuracao.getNome() + "' neste jogo");
        }
        siglaValidationService.validarSiglaDisponivel(
            configuracao.getSigla(),
            configuracao.getJogo().getId(),
            null,
            TipoSigla.VANTAGEM
        );
        validarFormulaCusto(configuracao.getFormulaCusto());
    }

    @Override
    protected void validarAntesAtualizar(VantagemConfig configuracaoExistente, VantagemConfig configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equalsIgnoreCase(configuracaoAtualizada.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCase(configuracaoExistente.getJogo().getId(), configuracaoAtualizada.getNome())) {
                throw new ConflictException("Já existe uma vantagem com o nome '" + configuracaoAtualizada.getNome() + "' neste jogo");
            }
        }
        String siglaNova = configuracaoAtualizada.getSigla();
        if (siglaNova != null && !siglaNova.equalsIgnoreCase(configuracaoExistente.getSigla())) {
            siglaValidationService.validarSiglaDisponivel(
                siglaNova,
                configuracaoExistente.getJogo().getId(),
                configuracaoExistente.getId(),
                TipoSigla.VANTAGEM
            );
        }
        validarFormulaCusto(configuracaoAtualizada.getFormulaCusto());
    }

    /**
     * Adiciona um pré-requisito a uma vantagem, validando auto-referência, duplicata, jogo e ciclos.
     */
    @Transactional
    public VantagemPreRequisito adicionarPreRequisito(Long vantagemId, Long requisitoId, Integer nivelMinimo) {
        VantagemConfig vantagem = buscarPorId(vantagemId);
        VantagemConfig requisito = buscarPorId(requisitoId);

        if (!vantagem.getJogo().getId().equals(requisito.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.VantagemPreRequisito.JOGOS_DIFERENTES);
        }
        if (prerequisitoRepository.existsByVantagemIdAndRequisitoId(vantagemId, requisitoId)) {
            throw new ConflictException(ValidationMessages.VantagemPreRequisito.JA_EXISTE);
        }
        verificarCiclo(vantagemId, requisitoId);

        VantagemPreRequisito pr = VantagemPreRequisito.builder()
            .vantagem(vantagem)
            .requisito(requisito)
            .nivelMinimo(nivelMinimo != null ? nivelMinimo : 1)
            .build();
        return prerequisitoRepository.save(pr);
    }

    /**
     * Lista pré-requisitos de uma vantagem.
     */
    public List<VantagemPreRequisito> listarPreRequisitos(Long vantagemId) {
        return prerequisitoRepository.findByVantagemId(vantagemId);
    }

    /**
     * Remove um pré-requisito (hard delete).
     */
    @Transactional
    public void removerPreRequisito(Long prId) {
        VantagemPreRequisito pr = prerequisitoRepository.findById(prId)
            .orElseThrow(() -> new ResourceNotFoundException("VantagemPreRequisito", prId));
        prerequisitoRepository.delete(pr);
    }

    private void verificarCiclo(Long vantagemId, Long requisitoId) {
        if (vantagemId.equals(requisitoId)) {
            throw new ConflictException(ValidationMessages.VantagemPreRequisito.AUTO_REFERENCIA);
        }

        // DFS: percorre transitivamente o que 'requisitoId' exige
        Set<Long> visitados = new HashSet<>();
        Deque<Long> fila = new ArrayDeque<>();
        fila.push(requisitoId);

        while (!fila.isEmpty()) {
            Long atual = fila.pop();
            if (!visitados.add(atual)) continue;

            List<Long> requisitosDoAtual = prerequisitoRepository.findByVantagemId(atual)
                .stream()
                .map(p -> p.getRequisito().getId())
                .toList();

            for (Long req : requisitosDoAtual) {
                if (req.equals(vantagemId)) {
                    throw new ConflictException(ValidationMessages.VantagemPreRequisito.CICLO_DETECTADO);
                }
                fila.push(req);
            }
        }
    }

    private void validarFormulaCusto(String formula) {
        if (formula == null || formula.isBlank()) return;
        FormulaValidationResult result = formulaEvaluatorService.validarFormula(
            formula, Set.of("custo_base", "nivel_vantagem")
        );
        if (!result.valid()) {
            String msg = result.erroSintaxe() != null
                ? ValidationMessages.VantagemConfig.FORMULA_CUSTO_SINTAXE_INVALIDA + ": " + result.erroSintaxe()
                : ValidationMessages.VantagemConfig.FORMULA_CUSTO_VARIAVEIS_INVALIDAS
                    .formatted(String.join(", ", result.variaveisInvalidas()));
            throw new ValidationException(msg);
        }
    }

    @Override
    protected void atualizarCampos(VantagemConfig existente, VantagemConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setSigla(atualizado.getSigla());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        existente.setNivelMaximo(atualizado.getNivelMaximo());
        existente.setFormulaCusto(atualizado.getFormulaCusto());
        existente.setDescricaoEfeito(atualizado.getDescricaoEfeito());
        existente.setCategoriaVantagem(atualizado.getCategoriaVantagem());
        if (atualizado.getTipoVantagem() != null) {
            existente.setTipoVantagem(atualizado.getTipoVantagem());
        }
    }
}
