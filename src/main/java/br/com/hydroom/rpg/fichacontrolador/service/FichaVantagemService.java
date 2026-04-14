package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVantagem;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.OrigemVantagem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoVantagem;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVantagemRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemPreRequisitoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de vantagens de fichas.
 *
 * <p>Regras de negocio:</p>
 * <ul>
 *   <li>Vantagens do tipo VANTAGEM sao compradas com custo de pontos</li>
 *   <li>Vantagens do tipo INSOLITUS sao concedidas pelo Mestre sem custo de pontos</li>
 *   <li>Pre-requisitos devem ser atendidos antes de comprar (exceto Insolitus)</li>
 *   <li>Nivel nao pode ultrapassar o nivelMaximo</li>
 *   <li>Mestre pode revogar qualquer vantagem (soft delete)</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaVantagemService {

    private final FichaRepository fichaRepository;
    private final FichaVantagemRepository fichaVantagemRepository;
    private final VantagemConfigRepository vantagemConfigRepository;
    private final VantagemPreRequisitoRepository vantagemPreRequisitoRepository;
    private final FichaAtributoRepository fichaAtributoRepository;
    private final FichaAptidaoRepository fichaAptidaoRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;
    private final FormulaEvaluatorService formulaEvaluatorService;

    /**
     * Compra uma vantagem para uma ficha.
     *
     * @param fichaId ID da ficha
     * @param vantagemConfigId ID da configuração de vantagem
     * @return FichaVantagem criada
     */
    @Transactional
    public FichaVantagem comprar(Long fichaId, Long vantagemConfigId) {
        // 1. Verificar se já existe
        fichaVantagemRepository.findByFichaIdAndVantagemConfigId(fichaId, vantagemConfigId)
                .ifPresent(v -> {
                    throw new ConflictException("A ficha já possui esta vantagem: " + vantagemConfigId);
                });

        // 2. Buscar ficha
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        // 3. Verificar acesso de escrita
        verificarAcessoEscrita(ficha);

        // 4. Buscar VantagemConfig
        VantagemConfig vantagemConfig = vantagemConfigRepository.findById(vantagemConfigId)
                .orElseThrow(() -> new ResourceNotFoundException("Vantagem não encontrada: " + vantagemConfigId));

        // 5. Verificar pré-requisitos (lógica polimórfica OR-por-tipo AND-entre-tipos)
        List<VantagemPreRequisito> preRequisitos = vantagemPreRequisitoRepository.findByVantagemId(vantagemConfigId);
        if (!preRequisitos.isEmpty()) {
            verificarPreRequisitosPolimorficos(ficha, vantagemConfig.getNome(), preRequisitos);
        }

        // 6. Calcular custo para nível 1
        int custo = formulaEvaluatorService.calcularCustoVantagem(
                vantagemConfig.getFormulaCusto(), 0, 1);

        // 7. Criar e salvar FichaVantagem
        FichaVantagem fichaVantagem = FichaVantagem.builder()
                .ficha(ficha)
                .vantagemConfig(vantagemConfig)
                .nivelAtual(1)
                .custoPago(custo)
                .build();

        fichaVantagem = fichaVantagemRepository.save(fichaVantagem);
        log.info("Vantagem '{}' comprada para ficha {} (custo: {})", vantagemConfig.getNome(), fichaId, custo);
        return fichaVantagem;
    }

    /**
     * Aumenta o nível de uma vantagem já comprada.
     *
     * @param fichaId ID da ficha
     * @param fichaVantagemId ID da FichaVantagem
     * @return FichaVantagem atualizada
     */
    @Transactional
    public FichaVantagem aumentarNivel(Long fichaId, Long fichaVantagemId) {
        // 1. Buscar FichaVantagem
        FichaVantagem fichaVantagem = fichaVantagemRepository.findById(fichaVantagemId)
                .orElseThrow(() -> new ResourceNotFoundException("FichaVantagem não encontrada: " + fichaVantagemId));

        // 2. Verificar que pertence à ficha correta
        if (!fichaVantagem.getFicha().getId().equals(fichaId)) {
            throw new ForbiddenException("Esta vantagem não pertence à ficha informada.");
        }

        // 3. Verificar acesso de escrita
        verificarAcessoEscrita(fichaVantagem.getFicha());

        // 4. Verificar se pode subir nível
        if (!fichaVantagem.podeSubirNivel()) {
            throw new ValidationException(
                    "A vantagem '" + fichaVantagem.getVantagemConfig().getNome()
                    + "' já está no nível máximo (" + fichaVantagem.getVantagemConfig().getNivelMaximo() + ")."
            );
        }

        // 5. Incrementar nível
        int novoNivel = fichaVantagem.getNivelAtual() + 1;
        fichaVantagem.setNivelAtual(novoNivel);

        // 6. Recalcular custo para o novo nível
        int novoCusto = formulaEvaluatorService.calcularCustoVantagem(
                fichaVantagem.getVantagemConfig().getFormulaCusto(), 0, novoNivel);
        fichaVantagem.setCustoPago(novoCusto);

        fichaVantagem = fichaVantagemRepository.save(fichaVantagem);
        log.info("Vantagem '{}' da ficha {} subiu para nível {} (custo: {})",
                fichaVantagem.getVantagemConfig().getNome(), fichaId, novoNivel, novoCusto);
        return fichaVantagem;
    }

    /**
     * Concede um Insolitus (vantagem especial) a uma ficha.
     * Apenas o Mestre pode conceder. Sem custo de pontos, sem verificacao de pre-requisitos.
     *
     * @param fichaId ID da ficha
     * @param vantagemConfigId ID da VantagemConfig (deve ser tipo INSOLITUS)
     * @return FichaVantagem criada
     */
    @Transactional
    public FichaVantagem concederInsolitus(Long fichaId, Long vantagemConfigId) {
        // 1. Verificar se ja existe
        fichaVantagemRepository.findByFichaIdAndVantagemConfigId(fichaId, vantagemConfigId)
                .ifPresent(v -> {
                    throw new ConflictException("A ficha já possui esta vantagem: " + vantagemConfigId);
                });

        // 2. Buscar ficha
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        // 3. Verificar que usuario e Mestre
        verificarAcessoMestre(ficha);

        // 4. Buscar VantagemConfig
        VantagemConfig vantagemConfig = vantagemConfigRepository.findById(vantagemConfigId)
                .orElseThrow(() -> new ResourceNotFoundException("Vantagem não encontrada: " + vantagemConfigId));

        // 5. Verificar que e do tipo INSOLITUS
        if (vantagemConfig.getTipoVantagem() != TipoVantagem.INSOLITUS) {
            throw new ValidationException(
                    "A vantagem '" + vantagemConfig.getNome() + "' não é do tipo Insólitus. "
                    + "Use o endpoint de compra para vantagens normais.");
        }

        // 6. Criar FichaVantagem sem custo
        FichaVantagem fichaVantagem = FichaVantagem.builder()
                .ficha(ficha)
                .vantagemConfig(vantagemConfig)
                .nivelAtual(1)
                .custoPago(0)
                .concedidoPeloMestre(true)
                .origem(OrigemVantagem.MESTRE)
                .build();

        fichaVantagem = fichaVantagemRepository.save(fichaVantagem);
        log.info("Insólitus '{}' concedido pelo Mestre para ficha {} (custo: 0)",
                vantagemConfig.getNome(), fichaId);
        return fichaVantagem;
    }

    /**
     * Revoga uma vantagem de uma ficha (soft delete).
     * Apenas o Mestre pode revogar qualquer vantagem (inclusive Insolitus).
     *
     * @param fichaId ID da ficha
     * @param fichaVantagemId ID da FichaVantagem
     */
    @Transactional
    public void revogarVantagem(Long fichaId, Long fichaVantagemId) {
        // 1. Buscar FichaVantagem
        FichaVantagem fichaVantagem = fichaVantagemRepository.findById(fichaVantagemId)
                .orElseThrow(() -> new ResourceNotFoundException("FichaVantagem não encontrada: " + fichaVantagemId));

        // 2. Verificar que pertence a ficha correta
        if (!fichaVantagem.getFicha().getId().equals(fichaId)) {
            throw new ForbiddenException("Esta vantagem não pertence à ficha informada.");
        }

        // 3. Verificar que usuario e Mestre
        verificarAcessoMestre(fichaVantagem.getFicha());

        // 4. Soft delete
        fichaVantagem.delete();
        fichaVantagemRepository.save(fichaVantagem);
        log.info("Vantagem '{}' revogada da ficha {} pelo Mestre",
                fichaVantagem.getVantagemConfig().getNome(), fichaId);
    }

    /**
     * Lista todas as vantagens de uma ficha.
     *
     * @param fichaId ID da ficha
     * @return Lista de FichaVantagem
     */
    public List<FichaVantagem> listar(Long fichaId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoLeitura(ficha);

        return fichaVantagemRepository.findByFichaIdWithConfig(fichaId);
    }

    // ==================== PRIVADOS ====================

    /**
     * Verifica os pré-requisitos polimórficos.
     *
     * <p>Regra:
     * <ul>
     *   <li>Pré-requisitos do <b>mesmo tipo</b> = OR (basta atender qualquer um do grupo)</li>
     *   <li>Pré-requisitos de <b>tipos diferentes</b> = AND (todos os grupos devem ser atendidos)</li>
     * </ul>
     */
    private void verificarPreRequisitosPolimorficos(Ficha ficha, String nomeVantagem,
                                                    List<VantagemPreRequisito> preRequisitos) {
        Map<TipoPreRequisito, List<VantagemPreRequisito>> gruposPorTipo = preRequisitos.stream()
            .collect(Collectors.groupingBy(VantagemPreRequisito::getTipo));

        for (Map.Entry<TipoPreRequisito, List<VantagemPreRequisito>> grupo : gruposPorTipo.entrySet()) {
            boolean grupoAtendido = grupo.getValue().stream()
                .anyMatch(prereq -> verificarPreRequisito(ficha, prereq));

            if (!grupoAtendido) {
                String descricaoGrupo = construirDescricaoGrupo(grupo.getKey(), grupo.getValue());
                throw new ValidationException(
                    "Pré-requisito não atendido para comprar '" + nomeVantagem + "': " + descricaoGrupo
                );
            }
        }
    }

    private boolean verificarPreRequisito(Ficha ficha, VantagemPreRequisito prereq) {
        return switch (prereq.getTipo()) {
            case VANTAGEM -> verificarPreRequisitoVantagem(ficha.getId(), prereq);
            case RACA -> verificarPreRequisitoRaca(ficha, prereq);
            case CLASSE -> verificarPreRequisitoClasse(ficha, prereq);
            case ATRIBUTO -> verificarPreRequisitoAtributo(ficha.getId(), prereq);
            case NIVEL -> verificarPreRequisitoNivel(ficha, prereq);
            case APTIDAO -> verificarPreRequisitoAptidao(ficha.getId(), prereq);
        };
    }

    private boolean verificarPreRequisitoVantagem(Long fichaId, VantagemPreRequisito prereq) {
        return fichaVantagemRepository
            .findByFichaIdAndVantagemConfigId(fichaId, prereq.getRequisito().getId())
            .map(fv -> fv.getNivelAtual() >= prereq.getNivelMinimo())
            .orElse(false);
    }

    private boolean verificarPreRequisitoRaca(Ficha ficha, VantagemPreRequisito prereq) {
        return ficha.getRaca() != null
            && ficha.getRaca().getId().equals(prereq.getRaca().getId());
    }

    private boolean verificarPreRequisitoClasse(Ficha ficha, VantagemPreRequisito prereq) {
        return ficha.getClasse() != null
            && ficha.getClasse().getId().equals(prereq.getClasse().getId());
    }

    private boolean verificarPreRequisitoAtributo(Long fichaId, VantagemPreRequisito prereq) {
        return fichaAtributoRepository
            .findByFichaIdAndAtributoConfigId(fichaId, prereq.getAtributo().getId())
            .map(fa -> fa.getBase() >= prereq.getValorMinimo())
            .orElse(false);
    }

    private boolean verificarPreRequisitoNivel(Ficha ficha, VantagemPreRequisito prereq) {
        return ficha.getNivel() >= prereq.getValorMinimo();
    }

    private boolean verificarPreRequisitoAptidao(Long fichaId, VantagemPreRequisito prereq) {
        return fichaAptidaoRepository
            .findByFichaIdAndAptidaoConfigId(fichaId, prereq.getAptidao().getId())
            .map(fa -> fa.getBase() >= prereq.getValorMinimo())
            .orElse(false);
    }

    private String construirDescricaoGrupo(TipoPreRequisito tipo, List<VantagemPreRequisito> grupo) {
        String itens = grupo.stream()
            .map(pr -> switch (tipo) {
                case VANTAGEM -> "'" + pr.getRequisito().getNome() + "' nível " + pr.getNivelMinimo();
                case RACA -> "Raça " + pr.getRaca().getNome();
                case CLASSE -> "Classe " + pr.getClasse().getNome();
                case ATRIBUTO -> pr.getAtributo().getNome() + " >= " + pr.getValorMinimo();
                case NIVEL -> "Nível >= " + pr.getValorMinimo();
                case APTIDAO -> pr.getAptidao().getNome() + " >= " + pr.getValorMinimo();
            })
            .collect(Collectors.joining(" ou "));
        return itens;
    }

    private void verificarAcessoLeitura(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return;
        }

        // NPCs só são visíveis para o Mestre
        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só são acessíveis pelo Mestre.");
        }

        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
        }
    }

    private void verificarAcessoMestre(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode realizar esta operação.");
        }
    }

    private void verificarAcessoEscrita(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return;
        }

        // NPCs só podem ser modificados pelo Mestre
        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só podem ser modificados pelo Mestre.");
        }

        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você só pode modificar suas próprias fichas.");
        }
    }

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
    }
}
