package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemAdicionarRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemCustomizadoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemDuracaoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaItem;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaItemRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service para gerenciamento do inventário de itens de uma ficha.
 *
 * <p>Regras de negócio:</p>
 * <ul>
 *   <li>Jogador pode adicionar itens cuja raridade permita (podeJogadorAdicionar=true)</li>
 *   <li>Mestre pode adicionar/forçar qualquer item e criar itens customizados</li>
 *   <li>Requisitos de nível são validados ao adicionar (exceto se Mestre forçar)</li>
 *   <li>Item com duracaoAtual == 0 (quebrado) não pode ser equipado</li>
 *   <li>Durabilidade decrementada a 0 causa auto-desequipe</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaItemService {

    private final FichaRepository fichaRepository;
    private final FichaItemRepository fichaItemRepository;
    private final ItemConfigRepository itemConfigRepository;
    private final RaridadeItemConfigRepository raridadeItemConfigRepository;
    private final FichaAtributoRepository fichaAtributoRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;
    private final FichaService fichaService;

    /**
     * Lista o inventário completo de uma ficha, separado por equipados e em estoque.
     *
     * @param fichaId         ID da ficha
     * @param usuarioAtualId  ID do usuário solicitante
     * @return lista de itens equipados, inventário, peso total e capacidade de carga
     */
    public List<FichaItem> listarItens(Long fichaId, Long usuarioAtualId) {
        Ficha ficha = buscarFicha(fichaId);
        verificarAcessoLeitura(ficha, usuarioAtualId);
        return fichaItemRepository.findByFichaIdWithDetails(fichaId);
    }

    /**
     * Calcula o peso total do inventário (equipados + não equipados).
     */
    public BigDecimal calcularPesoTotal(List<FichaItem> itens) {
        return itens.stream()
                .map(item -> calcularPesoEfetivo(item)
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula o peso efetivo de um item: fichaItem.peso ?? itemConfig.peso ?? ZERO.
     */
    public BigDecimal calcularPesoEfetivo(FichaItem item) {
        if (item.getPeso() != null) {
            return item.getPeso();
        }
        if (item.getItemConfig() != null && item.getItemConfig().getPeso() != null) {
            return item.getItemConfig().getPeso();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calcula a capacidade de carga da ficha (FOR.total * 3).
     * Retorna ZERO se não houver atributo FOR configurado.
     */
    public BigDecimal calcularCapacidadeCarga(Long fichaId) {
        // TODO [Spec 016 T5]: integrar capacidade de carga usando FichaAtributo de FOR quando FichaCalculationService estiver disponível
        // Tentativa de buscar atributo FOR para calcular capacidade de carga: FOR.total * 3
        return fichaAtributoRepository.findByFichaIdWithConfig(fichaId).stream()
                .filter(fa -> "FOR".equalsIgnoreCase(fa.getAtributoConfig().getAbreviacao()))
                .findFirst()
                .map(fa -> BigDecimal.valueOf(fa.getTotal()).multiply(BigDecimal.valueOf(3)))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Adiciona um item de catálogo ao inventário de uma ficha.
     *
     * @param fichaId         ID da ficha
     * @param request         dados do item a adicionar
     * @param usuarioAtualId  ID do usuário solicitante
     * @return FichaItem criado
     */
    @Transactional
    public FichaItem adicionarItem(Long fichaId, FichaItemAdicionarRequest request,
                                   Long usuarioAtualId) {
        Ficha ficha = buscarFicha(fichaId);
        boolean isMestre = isMestreDoJogo(ficha.getJogo().getId(), usuarioAtualId);
        verificarAcessoEscrita(ficha, usuarioAtualId, isMestre);

        ItemConfig itemConfig = itemConfigRepository.findById(request.itemConfigId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ItemConfig não encontrado: " + request.itemConfigId()));

        // Verificar que item pertence ao mesmo jogo da ficha
        if (!itemConfig.getJogo().getId().equals(ficha.getJogo().getId())) {
            throw new ValidationException("Item não pertence ao jogo desta ficha.");
        }

        // Jogador só pode adicionar itens cuja raridade permita
        if (!isMestre && !itemConfig.getRaridade().isPodeJogadorAdicionar()) {
            throw new ForbiddenException(
                    "Jogadores não podem adicionar itens de raridade '"
                    + itemConfig.getRaridade().getNome() + "' ao inventário.");
        }

        // Validar requisitos (Mestre com forcarAdicao pode pular)
        if (!isMestre || !request.forcarAdicao()) {
            validarRequisitos(itemConfig, ficha);
        }

        Usuario usuarioAtual = buscarUsuario(usuarioAtualId);

        FichaItem fichaItem = FichaItem.builder()
                .ficha(ficha)
                .itemConfig(itemConfig)
                .nome(itemConfig.getNome())
                .quantidade(request.quantidade())
                .notas(request.notas())
                .duracaoAtual(itemConfig.getDuracaoPadrao())
                .adicionadoPor(usuarioAtual.getEmail())
                .build();

        fichaItem = fichaItemRepository.save(fichaItem);
        log.info("Item '{}' adicionado ao inventário da ficha {} por {}", fichaItem.getNome(), fichaId, usuarioAtualId);
        return fichaItem;
    }

    /**
     * Adiciona um item customizado (sem itemConfig) ao inventário.
     * Exclusivo do Mestre.
     *
     * @param fichaId         ID da ficha
     * @param request         dados do item customizado
     * @param usuarioAtualId  ID do Mestre solicitante
     * @return FichaItem criado
     */
    @Transactional
    public FichaItem adicionarItemCustomizado(Long fichaId, FichaItemCustomizadoRequest request,
                                              Long usuarioAtualId) {
        Ficha ficha = buscarFicha(fichaId);
        verificarAcessoMestre(ficha, usuarioAtualId);

        RaridadeItemConfig raridade = raridadeItemConfigRepository.findById(request.raridadeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Raridade não encontrada: " + request.raridadeId()));

        // Verificar que a raridade pertence ao mesmo jogo
        if (!raridade.getJogo().getId().equals(ficha.getJogo().getId())) {
            throw new ValidationException("Raridade não pertence ao jogo desta ficha.");
        }

        Usuario usuarioAtual = buscarUsuario(usuarioAtualId);

        FichaItem fichaItem = FichaItem.builder()
                .ficha(ficha)
                .itemConfig(null)
                .nome(request.nome())
                .raridade(raridade)
                .peso(request.peso())
                .quantidade(request.quantidade())
                .notas(request.notas())
                .adicionadoPor(usuarioAtual.getEmail())
                .build();

        fichaItem = fichaItemRepository.save(fichaItem);
        log.info("Item customizado '{}' adicionado ao inventário da ficha {} pelo Mestre {}", fichaItem.getNome(), fichaId, usuarioAtualId);
        return fichaItem;
    }

    /**
     * Equipa um item do inventário da ficha.
     *
     * @param fichaId         ID da ficha
     * @param itemId          ID do FichaItem
     * @param usuarioAtualId  ID do usuário solicitante
     * @return FichaItem atualizado
     */
    @Transactional
    public FichaItem equiparItem(Long fichaId, Long itemId, Long usuarioAtualId) {
        FichaItem fichaItem = buscarFichaItem(fichaId, itemId);
        verificarAcessoEscritaItem(fichaItem, usuarioAtualId);

        if (fichaItem.getDuracaoAtual() != null && fichaItem.getDuracaoAtual() == 0) {
            throw new BusinessException("Item quebrado não pode ser equipado.");
        }

        fichaItem.setEquipado(true);
        fichaItem = fichaItemRepository.save(fichaItem);

        fichaService.recalcularFicha(fichaId);

        log.info("Item '{}' equipado na ficha {}", fichaItem.getNome(), fichaId);
        return fichaItem;
    }

    /**
     * Desequipa um item do inventário da ficha.
     *
     * @param fichaId         ID da ficha
     * @param itemId          ID do FichaItem
     * @param usuarioAtualId  ID do usuário solicitante
     * @return FichaItem atualizado
     */
    @Transactional
    public FichaItem desequiparItem(Long fichaId, Long itemId, Long usuarioAtualId) {
        FichaItem fichaItem = buscarFichaItem(fichaId, itemId);
        verificarAcessoEscritaItem(fichaItem, usuarioAtualId);

        fichaItem.setEquipado(false);
        fichaItem = fichaItemRepository.save(fichaItem);

        fichaService.recalcularFicha(fichaId);

        log.info("Item '{}' desequipado da ficha {}", fichaItem.getNome(), fichaId);
        return fichaItem;
    }

    /**
     * Decrementa ou restaura a durabilidade de um item. Exclusivo do Mestre.
     *
     * @param fichaId         ID da ficha
     * @param itemId          ID do FichaItem
     * @param request         dados de durabilidade
     * @param usuarioAtualId  ID do Mestre solicitante
     * @return FichaItem atualizado
     */
    @Transactional
    public FichaItem decrementarDurabilidade(Long fichaId, Long itemId,
                                              FichaItemDuracaoRequest request,
                                              Long usuarioAtualId) {
        FichaItem fichaItem = buscarFichaItem(fichaId, itemId);
        verificarAcessoMestre(fichaItem.getFicha(), usuarioAtualId);

        boolean estaEquipadoAntes = fichaItem.isEquipado();

        if (request.restaurar()) {
            Integer duracaoPadrao = fichaItem.getItemConfig() != null
                    ? fichaItem.getItemConfig().getDuracaoPadrao()
                    : null;
            fichaItem.setDuracaoAtual(duracaoPadrao);
            log.info("Durabilidade do item '{}' restaurada na ficha {}", fichaItem.getNome(), fichaId);
        } else {
            int duracaoAtual = fichaItem.getDuracaoAtual() != null ? fichaItem.getDuracaoAtual() : 0;
            int novaDuracao = Math.max(0, duracaoAtual - request.decremento());
            fichaItem.setDuracaoAtual(novaDuracao);

            if (novaDuracao == 0 && fichaItem.isEquipado()) {
                fichaItem.setEquipado(false);
                log.info("Item '{}' da ficha {} chegou a durabilidade 0 e foi desequipado automaticamente.",
                        fichaItem.getNome(), fichaId);
            }
        }

        fichaItem = fichaItemRepository.save(fichaItem);

        // Recalcula se o estado de equipado mudou (auto-desequipe por durabilidade 0)
        if (estaEquipadoAntes && !fichaItem.isEquipado()) {
            fichaService.recalcularFicha(fichaId);
        }

        return fichaItem;
    }

    /**
     * Remove (soft delete) um item do inventário da ficha.
     *
     * @param fichaId         ID da ficha
     * @param itemId          ID do FichaItem
     * @param usuarioAtualId  ID do usuário solicitante
     */
    @Transactional
    public void removerItem(Long fichaId, Long itemId, Long usuarioAtualId) {
        FichaItem fichaItem = buscarFichaItem(fichaId, itemId);
        verificarAcessoEscritaItem(fichaItem, usuarioAtualId);

        boolean isMestre = isMestreDoJogo(fichaItem.getFicha().getJogo().getId(), usuarioAtualId);

        if (!isMestre) {
            // TODO [Spec 016 T5 / ClasseEquipamentoInicial]: verificar se item é obrigatório de classe inicial
            // Se ClasseEquipamentoInicial.obrigatorio == true, jogador não pode remover
        }

        boolean estaEquipado = fichaItem.isEquipado();
        fichaItem.delete();
        fichaItemRepository.save(fichaItem);

        if (estaEquipado) {
            fichaService.recalcularFicha(fichaId);
        }

        log.info("Item '{}' removido do inventário da ficha {} por {}", fichaItem.getNome(), fichaId, usuarioAtualId);
    }

    // ==================== PRIVADOS ====================

    private Ficha buscarFicha(Long fichaId) {
        return fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + usuarioId));
    }

    private FichaItem buscarFichaItem(Long fichaId, Long itemId) {
        FichaItem fichaItem = fichaItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado: " + itemId));
        if (!fichaItem.getFicha().getId().equals(fichaId)) {
            throw new ForbiddenException("Este item não pertence à ficha informada.");
        }
        return fichaItem;
    }

    private void validarRequisitos(ItemConfig itemConfig, Ficha ficha) {
        List<String> requisitosNaoAtendidos = new ArrayList<>();
        for (ItemRequisito requisito : itemConfig.getRequisitos()) {
            if (requisito.getTipo() == TipoRequisito.NIVEL) {
                int nivelMinimo = requisito.getValorMinimo() != null ? requisito.getValorMinimo() : 1;
                if (ficha.getNivel() < nivelMinimo) {
                    requisitosNaoAtendidos.add("Nível mínimo: " + nivelMinimo
                            + " (ficha possui nível " + ficha.getNivel() + ")");
                }
            } else {
                // TODO [Spec 016 T5]: validar outros tipos de requisito (ATRIBUTO, CLASSE, RACA, etc.)
                log.debug("Requisito do tipo {} ignorado na validação (não implementado)", requisito.getTipo());
            }
        }
        if (!requisitosNaoAtendidos.isEmpty()) {
            throw new ValidationException("Requisitos não atendidos para adicionar '"
                    + itemConfig.getNome() + "': " + String.join(", ", requisitosNaoAtendidos));
        }
    }

    private boolean isMestreDoJogo(Long jogoId, Long usuarioId) {
        return jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioId, RoleJogo.MESTRE);
    }

    private void verificarAcessoLeitura(Ficha ficha, Long usuarioAtualId) {
        Long jogoId = ficha.getJogo().getId();
        boolean isMestre = isMestreDoJogo(jogoId, usuarioAtualId);
        if (isMestre) return;

        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só são acessíveis pelo Mestre.");
        }
        if (!usuarioAtualId.equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
        }
    }

    private void verificarAcessoMestre(Ficha ficha, Long usuarioAtualId) {
        Long jogoId = ficha.getJogo().getId();
        if (!isMestreDoJogo(jogoId, usuarioAtualId)) {
            throw new ForbiddenException("Apenas o Mestre pode realizar esta operação.");
        }
    }

    private void verificarAcessoEscrita(Ficha ficha, Long usuarioAtualId, boolean isMestre) {
        if (isMestre) return;
        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só podem ser modificados pelo Mestre.");
        }
        if (!usuarioAtualId.equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você só pode modificar suas próprias fichas.");
        }
    }

    private void verificarAcessoEscritaItem(FichaItem fichaItem, Long usuarioAtualId) {
        Ficha ficha = fichaItem.getFicha();
        Long jogoId = ficha.getJogo().getId();
        boolean isMestre = isMestreDoJogo(jogoId, usuarioAtualId);
        if (isMestre) return;
        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só podem ser modificados pelo Mestre.");
        }
        if (!usuarioAtualId.equals(ficha.getJogadorId())) {
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
