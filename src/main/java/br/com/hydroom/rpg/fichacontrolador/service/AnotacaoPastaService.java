package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarPastaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarPastaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.AnotacaoPastaResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.mapper.AnotacaoPastaMapper;
import br.com.hydroom.rpg.fichacontrolador.model.AnotacaoPasta;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.AnotacaoPastaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de pastas de anotações de fichas.
 *
 * <p>Regras de acesso:</p>
 * <ul>
 *   <li>MESTRE pode criar/editar/deletar pastas em qualquer ficha do jogo</li>
 *   <li>JOGADOR pode criar/editar/deletar pastas apenas em sua própria ficha</li>
 *   <li>NPCs só são acessíveis pelo MESTRE</li>
 * </ul>
 *
 * <p>Regras hierárquicas:</p>
 * <ul>
 *   <li>Máximo 3 níveis de hierarquia (raiz = nível 1, subpasta = nível 2, sub-subpasta = nível 3)</li>
 *   <li>Nome deve ser único dentro do mesmo pai e ficha</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnotacaoPastaService {

    private final AnotacaoPastaRepository anotacaoPastaRepository;
    private final FichaRepository fichaRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AnotacaoPastaMapper anotacaoPastaMapper;

    /**
     * Lista as pastas de uma ficha como árvore hierárquica.
     *
     * @param fichaId         ID da ficha
     * @param usuarioAtualId  ID do usuário solicitante
     * @return lista de pastas raiz com sub-pastas aninhadas
     */
    public List<AnotacaoPastaResponse> listarArvore(Long fichaId, Long usuarioAtualId) {
        Ficha ficha = buscarFichaComAcesso(fichaId, usuarioAtualId);

        List<AnotacaoPasta> todas = anotacaoPastaRepository.findByFichaIdOrderByOrdemExibicaoAsc(ficha.getId());

        return montarArvore(todas);
    }

    /**
     * Cria uma nova pasta para uma ficha.
     *
     * @param fichaId         ID da ficha
     * @param request         dados da pasta a criar
     * @param usuarioAtualId  ID do usuário solicitante
     * @return pasta criada como response
     */
    @Transactional
    public AnotacaoPastaResponse criar(Long fichaId, CriarPastaRequest request, Long usuarioAtualId) {
        Ficha ficha = buscarFichaComAcesso(fichaId, usuarioAtualId);

        AnotacaoPasta pastaPai = null;

        if (request.pastaPaiId() != null) {
            pastaPai = anotacaoPastaRepository.findById(request.pastaPaiId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pasta pai não encontrada: " + request.pastaPaiId()));

            if (!pastaPai.getFicha().getId().equals(fichaId)) {
                throw new ForbiddenException("A pasta pai não pertence a esta ficha.");
            }

            int nivelPai = calcularNivel(pastaPai);
            if (nivelPai >= 3) {
                throw new BusinessException("Nível máximo de hierarquia atingido (3).");
            }
        }

        validarNomeDuplicado(fichaId, pastaPai != null ? pastaPai.getId() : null, request.nome());

        int ordem = request.ordemExibicao() != null ? request.ordemExibicao() : 0;

        AnotacaoPasta pasta = AnotacaoPasta.builder()
                .ficha(ficha)
                .nome(request.nome())
                .pastaPai(pastaPai)
                .ordemExibicao(ordem)
                .build();

        pasta = anotacaoPastaRepository.save(pasta);
        log.info("Pasta '{}' criada na ficha {} por usuário {}", pasta.getNome(), fichaId, usuarioAtualId);

        AnotacaoPastaResponse response = anotacaoPastaMapper.toResponse(pasta);
        return new AnotacaoPastaResponse(
                response.id(), response.fichaId(), response.nome(), response.pastaPaiId(),
                response.ordemExibicao(), List.of(), response.dataCriacao(), response.dataUltimaAtualizacao()
        );
    }

    /**
     * Atualiza nome e/ou ordem de exibição de uma pasta.
     *
     * @param fichaId         ID da ficha
     * @param pastaId         ID da pasta a atualizar
     * @param request         dados a atualizar
     * @param usuarioAtualId  ID do usuário solicitante
     * @return pasta atualizada como response
     */
    @Transactional
    public AnotacaoPastaResponse atualizar(Long fichaId, Long pastaId, AtualizarPastaRequest request,
                                           Long usuarioAtualId) {
        buscarFichaComAcesso(fichaId, usuarioAtualId);

        AnotacaoPasta pasta = buscarPastaVerificandoFicha(pastaId, fichaId);

        if (request.nome() != null && !request.nome().equals(pasta.getNome())) {
            Long pastaPaiId = pasta.getPastaPai() != null ? pasta.getPastaPai().getId() : null;
            validarNomeDuplicado(fichaId, pastaPaiId, request.nome());
        }

        anotacaoPastaMapper.atualizarEntidade(request, pasta);
        pasta = anotacaoPastaRepository.save(pasta);
        log.info("Pasta {} atualizada na ficha {} por usuário {}", pastaId, fichaId, usuarioAtualId);

        AnotacaoPastaResponse response = anotacaoPastaMapper.toResponse(pasta);
        return new AnotacaoPastaResponse(
                response.id(), response.fichaId(), response.nome(), response.pastaPaiId(),
                response.ordemExibicao(), List.of(), response.dataCriacao(), response.dataUltimaAtualizacao()
        );
    }

    /**
     * Deleta uma pasta (soft delete).
     *
     * <p>Sub-pastas diretas são desaninhadas para a raiz (pastaPai = null).
     * Anotações na pasta serão desvinculadas quando T1 adicionar o campo pastaPai em FichaAnotacao.</p>
     *
     * @param fichaId         ID da ficha
     * @param pastaId         ID da pasta a deletar
     * @param usuarioAtualId  ID do usuário solicitante
     */
    @Transactional
    public void deletar(Long fichaId, Long pastaId, Long usuarioAtualId) {
        buscarFichaComAcesso(fichaId, usuarioAtualId);

        AnotacaoPasta pasta = buscarPastaVerificandoFicha(pastaId, fichaId);

        // Desaninhar sub-pastas diretas: elas ficam na raiz
        List<AnotacaoPasta> subPastas = anotacaoPastaRepository.findByPastaPaiIdOrderByOrdemExibicaoAsc(pastaId);
        for (AnotacaoPasta sub : subPastas) {
            sub.setPastaPai(null);
            anotacaoPastaRepository.save(sub);
        }

        // TODO (T1): Desvincular FichaAnotacao que referenciam esta pasta quando o campo pastaPai
        //            for adicionado à entidade FichaAnotacao.

        pasta.delete();
        anotacaoPastaRepository.save(pasta);
        log.info("Pasta {} deletada da ficha {} por usuário {}", pastaId, fichaId, usuarioAtualId);
    }

    // ==================== PRIVADOS ====================

    /**
     * Busca a ficha e verifica se o usuário tem acesso a ela.
     * MESTRE acessa qualquer ficha do jogo. JOGADOR só acessa a própria.
     */
    private Ficha buscarFichaComAcesso(Long fichaId, Long usuarioAtualId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        Long jogoId = ficha.getJogo().getId();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtualId, RoleJogo.MESTRE);

        if (isMestre) {
            return ficha;
        }

        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só são acessíveis pelo Mestre.");
        }

        if (!usuarioAtualId.equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
        }

        return ficha;
    }

    private AnotacaoPasta buscarPastaVerificandoFicha(Long pastaId, Long fichaId) {
        AnotacaoPasta pasta = anotacaoPastaRepository.findById(pastaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pasta não encontrada: " + pastaId));

        if (!pasta.getFicha().getId().equals(fichaId)) {
            throw new ForbiddenException("Esta pasta não pertence à ficha informada.");
        }

        return pasta;
    }

    /**
     * Calcula o nível da pasta na hierarquia.
     * Raiz = 1, filho da raiz = 2, etc.
     */
    private int calcularNivel(AnotacaoPasta pasta) {
        int nivel = 1;
        AnotacaoPasta atual = pasta;
        while (atual.getPastaPai() != null) {
            nivel++;
            atual = anotacaoPastaRepository.findById(atual.getPastaPai().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pasta pai não encontrada ao calcular nível."));
        }
        return nivel;
    }

    private void validarNomeDuplicado(Long fichaId, Long pastaPaiId, String nome) {
        boolean duplicado;
        if (pastaPaiId == null) {
            duplicado = anotacaoPastaRepository.existsByFichaIdAndPastaPaiIsNullAndNome(fichaId, nome);
        } else {
            duplicado = anotacaoPastaRepository.existsByFichaIdAndPastaPaiIdAndNome(fichaId, pastaPaiId, nome);
        }
        if (duplicado) {
            throw new ConflictException(
                    "Já existe uma pasta com o nome '" + nome + "' neste local.");
        }
    }

    /**
     * Monta árvore hierárquica a partir de lista plana de pastas.
     * Pastas sem pai são raízes; as demais são agrupadas por pastaPai.id.
     */
    private List<AnotacaoPastaResponse> montarArvore(List<AnotacaoPasta> todas) {
        Map<Long, List<AnotacaoPasta>> porPai = todas.stream()
                .filter(p -> p.getPastaPai() != null)
                .collect(Collectors.groupingBy(p -> p.getPastaPai().getId()));

        List<AnotacaoPasta> raizes = todas.stream()
                .filter(p -> p.getPastaPai() == null)
                .toList();

        return raizes.stream()
                .map(r -> toResponseComSubPastas(r, porPai))
                .toList();
    }

    private AnotacaoPastaResponse toResponseComSubPastas(AnotacaoPasta pasta,
                                                          Map<Long, List<AnotacaoPasta>> porPai) {
        AnotacaoPastaResponse base = anotacaoPastaMapper.toResponse(pasta);

        List<AnotacaoPastaResponse> subPastas = porPai.getOrDefault(pasta.getId(), new ArrayList<>())
                .stream()
                .map(sub -> toResponseComSubPastas(sub, porPai))
                .toList();

        return new AnotacaoPastaResponse(
                base.id(), base.fichaId(), base.nome(), base.pastaPaiId(),
                base.ordemExibicao(), subPastas, base.dataCriacao(), base.dataUltimaAtualizacao()
        );
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
