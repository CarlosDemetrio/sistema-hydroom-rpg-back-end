package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.VantagemPreRequisitoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.FormulaValidationResult;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
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

    @Autowired
    private ConfiguracaoRacaRepository racaRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoRepository;

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

    /**
     * Cria um VantagemConfig, preenchendo {@code ordemExibicao} automaticamente
     * como {@code MAX + 1} se o valor não for informado (null ou 0).
     */
    @Override
    @Transactional
    public VantagemConfig criar(VantagemConfig configuracao) {
        if (configuracao.getOrdemExibicao() == null || configuracao.getOrdemExibicao() == 0) {
            configuracao.setOrdemExibicao(
                calcularProximaOrdemExibicao(configuracao.getJogo().getId(), "VantagemConfig"));
        }
        return super.criar(configuracao);
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
     * Adiciona um pré-requisito polimórfico a uma vantagem.
     *
     * <p>Delega para o método específico com base no {@code request.tipo()}.</p>
     */
    @Transactional
    public VantagemPreRequisito adicionarPreRequisito(Long vantagemId, VantagemPreRequisitoRequest request) {
        VantagemConfig vantagem = buscarPorId(vantagemId);
        Long jogoId = vantagem.getJogo().getId();

        return switch (request.tipo()) {
            case VANTAGEM -> adicionarPreRequisitoVantagem(vantagem, jogoId, request);
            case RACA -> adicionarPreRequisitoRaca(vantagem, jogoId, request);
            case CLASSE -> adicionarPreRequisitoClasse(vantagem, jogoId, request);
            case ATRIBUTO -> adicionarPreRequisitoAtributo(vantagem, jogoId, request);
            case NIVEL -> adicionarPreRequisitoNivel(vantagem, request);
            case APTIDAO -> adicionarPreRequisitoAptidao(vantagem, jogoId, request);
        };
    }

    /**
     * Sobrecarga para compatibilidade retroativa com o tipo VANTAGEM.
     * Mantida para testes existentes que passam os parâmetros diretamente.
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
            .tipo(TipoPreRequisito.VANTAGEM)
            .requisito(requisito)
            .nivelMinimo(nivelMinimo != null ? nivelMinimo : 1)
            .build();
        return prerequisitoRepository.save(pr);
    }

    private VantagemPreRequisito adicionarPreRequisitoVantagem(VantagemConfig vantagem, Long jogoId,
                                                               VantagemPreRequisitoRequest request) {
        if (request.requisitoId() == null) {
            throw new ValidationException("requisitoId é obrigatório para pré-requisito do tipo VANTAGEM.");
        }
        VantagemConfig requisito = buscarPorId(request.requisitoId());
        if (!jogoId.equals(requisito.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.VantagemPreRequisito.JOGOS_DIFERENTES);
        }
        if (prerequisitoRepository.existsByVantagemIdAndRequisitoId(vantagem.getId(), request.requisitoId())) {
            throw new ConflictException(ValidationMessages.VantagemPreRequisito.JA_EXISTE);
        }
        verificarCiclo(vantagem.getId(), request.requisitoId());

        return prerequisitoRepository.save(VantagemPreRequisito.builder()
            .vantagem(vantagem)
            .tipo(TipoPreRequisito.VANTAGEM)
            .requisito(requisito)
            .nivelMinimo(request.nivelMinimo() != null ? request.nivelMinimo() : 1)
            .build());
    }

    private VantagemPreRequisito adicionarPreRequisitoRaca(VantagemConfig vantagem, Long jogoId,
                                                           VantagemPreRequisitoRequest request) {
        if (request.racaId() == null) {
            throw new ValidationException("racaId é obrigatório para pré-requisito do tipo RACA.");
        }
        Raca raca = racaRepository.findById(request.racaId())
            .orElseThrow(() -> new ResourceNotFoundException("Raça", request.racaId()));
        if (!jogoId.equals(raca.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.VantagemPreRequisito.JOGOS_DIFERENTES);
        }

        return prerequisitoRepository.save(VantagemPreRequisito.builder()
            .vantagem(vantagem)
            .tipo(TipoPreRequisito.RACA)
            .raca(raca)
            .build());
    }

    private VantagemPreRequisito adicionarPreRequisitoClasse(VantagemConfig vantagem, Long jogoId,
                                                             VantagemPreRequisitoRequest request) {
        if (request.classeId() == null) {
            throw new ValidationException("classeId é obrigatório para pré-requisito do tipo CLASSE.");
        }
        ClassePersonagem classe = classeRepository.findById(request.classeId())
            .orElseThrow(() -> new ResourceNotFoundException("Classe", request.classeId()));
        if (!jogoId.equals(classe.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.VantagemPreRequisito.JOGOS_DIFERENTES);
        }

        return prerequisitoRepository.save(VantagemPreRequisito.builder()
            .vantagem(vantagem)
            .tipo(TipoPreRequisito.CLASSE)
            .classe(classe)
            .build());
    }

    private VantagemPreRequisito adicionarPreRequisitoAtributo(VantagemConfig vantagem, Long jogoId,
                                                               VantagemPreRequisitoRequest request) {
        if (request.atributoId() == null) {
            throw new ValidationException("atributoId é obrigatório para pré-requisito do tipo ATRIBUTO.");
        }
        if (request.valorMinimo() == null) {
            throw new ValidationException("valorMinimo é obrigatório para pré-requisito do tipo ATRIBUTO.");
        }
        AtributoConfig atributo = atributoRepository.findById(request.atributoId())
            .orElseThrow(() -> new ResourceNotFoundException("AtributoConfig", request.atributoId()));
        if (!jogoId.equals(atributo.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.VantagemPreRequisito.JOGOS_DIFERENTES);
        }

        return prerequisitoRepository.save(VantagemPreRequisito.builder()
            .vantagem(vantagem)
            .tipo(TipoPreRequisito.ATRIBUTO)
            .atributo(atributo)
            .valorMinimo(request.valorMinimo())
            .build());
    }

    private VantagemPreRequisito adicionarPreRequisitoNivel(VantagemConfig vantagem,
                                                            VantagemPreRequisitoRequest request) {
        if (request.valorMinimo() == null) {
            throw new ValidationException("valorMinimo é obrigatório para pré-requisito do tipo NIVEL.");
        }

        return prerequisitoRepository.save(VantagemPreRequisito.builder()
            .vantagem(vantagem)
            .tipo(TipoPreRequisito.NIVEL)
            .valorMinimo(request.valorMinimo())
            .build());
    }

    private VantagemPreRequisito adicionarPreRequisitoAptidao(VantagemConfig vantagem, Long jogoId,
                                                              VantagemPreRequisitoRequest request) {
        if (request.aptidaoId() == null) {
            throw new ValidationException("aptidaoId é obrigatório para pré-requisito do tipo APTIDAO.");
        }
        if (request.valorMinimo() == null) {
            throw new ValidationException("valorMinimo é obrigatório para pré-requisito do tipo APTIDAO.");
        }
        AptidaoConfig aptidao = aptidaoRepository.findById(request.aptidaoId())
            .orElseThrow(() -> new ResourceNotFoundException("AptidaoConfig", request.aptidaoId()));
        if (!jogoId.equals(aptidao.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.VantagemPreRequisito.JOGOS_DIFERENTES);
        }

        return prerequisitoRepository.save(VantagemPreRequisito.builder()
            .vantagem(vantagem)
            .tipo(TipoPreRequisito.APTIDAO)
            .aptidao(aptidao)
            .valorMinimo(request.valorMinimo())
            .build());
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
