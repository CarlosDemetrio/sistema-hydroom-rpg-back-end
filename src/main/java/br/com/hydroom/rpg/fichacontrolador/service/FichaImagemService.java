package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaImagemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaImagem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoImagem;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarImagemRequest;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaImagemRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service para gerenciamento da galeria de imagens de fichas.
 *
 * <p>Regras de acesso:</p>
 * <ul>
 *   <li>MESTRE acessa e opera em qualquer ficha do jogo</li>
 *   <li>JOGADOR acessa apenas a própria ficha (não-NPC)</li>
 * </ul>
 *
 * <p>Regras de negócio:</p>
 * <ul>
 *   <li>Máximo 20 imagens ativas por ficha</li>
 *   <li>Somente um AVATAR por ficha — ao adicionar novo, o anterior vira GALERIA</li>
 *   <li>urlCloudinary, publicId e tipoImagem são imutáveis após upload</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaImagemService {

    private static final int LIMITE_IMAGENS_POR_FICHA = 20;

    private final FichaImagemRepository fichaImagemRepository;
    private final FichaRepository fichaRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final CloudinaryUploadService cloudinaryUploadService;
    private final FichaImagemMapper fichaImagemMapper;

    /**
     * Lista todas as imagens de uma ficha, respeitando controle de acesso.
     * Ordena por tipo (AVATAR primeiro) e depois por ordemExibicao.
     *
     * @param fichaId       ID da ficha
     * @param usuarioAtualId ID do usuário solicitante
     * @return lista de imagens ordenada
     */
    public List<FichaImagem> listar(Long fichaId, Long usuarioAtualId) {
        Ficha ficha = buscarFichaOuLancar(fichaId);
        verificarAcesso(ficha, usuarioAtualId);
        return fichaImagemRepository.findByFichaIdOrderByTipoImagemAscOrdemExibicaoAsc(fichaId);
    }

    /**
     * Faz upload de nova imagem e vincula à ficha.
     *
     * <p>Se tipoImagem for AVATAR e já existir um, o anterior é convertido para GALERIA.</p>
     *
     * @param fichaId        ID da ficha
     * @param arquivo        arquivo de imagem recebido via multipart
     * @param tipoImagem     tipo da imagem (AVATAR ou GALERIA)
     * @param titulo         título opcional
     * @param usuarioAtualId ID do usuário solicitante
     * @return entidade FichaImagem persistida
     * @throws BusinessException        se o limite de 20 imagens for atingido
     * @throws IllegalArgumentException se o arquivo for inválido (tipo ou tamanho)
     * @throws RuntimeException         se o upload ao Cloudinary falhar (mapeado para 502)
     */
    @Transactional
    public FichaImagem adicionar(Long fichaId, MultipartFile arquivo, TipoImagem tipoImagem,
                                  String titulo, Long usuarioAtualId) {
        Ficha ficha = buscarFichaOuLancar(fichaId);
        verificarAcesso(ficha, usuarioAtualId);

        long totalAtual = fichaImagemRepository.countByFichaId(fichaId);
        if (totalAtual >= LIMITE_IMAGENS_POR_FICHA) {
            throw new BusinessException(
                    "Limite de " + LIMITE_IMAGENS_POR_FICHA + " imagens por ficha atingido");
        }

        String folder = "rpg-fichas/" + ficha.getJogo().getId() + "/fichas/" + fichaId;
        Map<String, String> uploadResult = cloudinaryUploadService.upload(arquivo, folder);

        if (TipoImagem.AVATAR.equals(tipoImagem)) {
            fichaImagemRepository.findByFichaIdAndTipoImagem(fichaId, TipoImagem.AVATAR)
                    .ifPresent(avatarAnterior -> {
                        avatarAnterior.setTipoImagem(TipoImagem.GALERIA);
                        fichaImagemRepository.save(avatarAnterior);
                        log.info("Avatar anterior (id={}) convertido para GALERIA na ficha {}", avatarAnterior.getId(), fichaId);
                    });
        }

        FichaImagem imagem = FichaImagem.builder()
                .ficha(ficha)
                .urlCloudinary(uploadResult.get("url"))
                .publicId(uploadResult.get("public_id"))
                .titulo(titulo)
                .tipoImagem(tipoImagem)
                .ordemExibicao(0)
                .build();

        imagem = fichaImagemRepository.save(imagem);
        log.info("Imagem {} adicionada à ficha {} pelo usuário {} (tipo={})",
                imagem.getId(), fichaId, usuarioAtualId, tipoImagem);
        return imagem;
    }

    /**
     * Atualiza metadados da imagem (título e ordemExibicao). Campos nulos são ignorados.
     * urlCloudinary, publicId e tipoImagem são imutáveis.
     *
     * @param fichaId        ID da ficha (validação de pertencimento)
     * @param imagemId       ID da imagem
     * @param request        campos a atualizar
     * @param usuarioAtualId ID do usuário solicitante
     * @return entidade atualizada
     */
    @Transactional
    public FichaImagem atualizar(Long fichaId, Long imagemId, AtualizarImagemRequest request,
                                  Long usuarioAtualId) {
        FichaImagem imagem = buscarImagemOuLancar(imagemId);
        verificarPertencimento(imagem, fichaId);
        verificarAcesso(imagem.getFicha(), usuarioAtualId);

        fichaImagemMapper.atualizarEntidade(request, imagem);

        imagem = fichaImagemRepository.save(imagem);
        log.info("Imagem {} atualizada na ficha {} pelo usuário {}", imagemId, fichaId, usuarioAtualId);
        return imagem;
    }

    /**
     * Deleta imagem: remove do Cloudinary (falha é logada, não propagada) e realiza soft delete local.
     *
     * @param fichaId        ID da ficha (validação de pertencimento)
     * @param imagemId       ID da imagem
     * @param usuarioAtualId ID do usuário solicitante
     */
    @Transactional
    public void deletar(Long fichaId, Long imagemId, Long usuarioAtualId) {
        FichaImagem imagem = buscarImagemOuLancar(imagemId);
        verificarPertencimento(imagem, fichaId);
        verificarAcesso(imagem.getFicha(), usuarioAtualId);

        try {
            cloudinaryUploadService.destroy(imagem.getPublicId());
        } catch (Exception e) {
            log.error("Falha ao remover imagem do Cloudinary (publicId={}): {}. Soft delete local prossegue.",
                    imagem.getPublicId(), e.getMessage());
        }

        imagem.delete();
        fichaImagemRepository.save(imagem);
        log.info("Imagem {} deletada da ficha {} pelo usuário {}", imagemId, fichaId, usuarioAtualId);
    }

    // ==================== PRIVADOS ====================

    private Ficha buscarFichaOuLancar(Long fichaId) {
        return fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));
    }

    private FichaImagem buscarImagemOuLancar(Long imagemId) {
        return fichaImagemRepository.findById(imagemId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada: " + imagemId));
    }

    private void verificarPertencimento(FichaImagem imagem, Long fichaId) {
        if (!imagem.getFicha().getId().equals(fichaId)) {
            throw new ForbiddenException("Imagem não pertence à ficha informada.");
        }
    }

    private void verificarAcesso(Ficha ficha, Long usuarioAtualId) {
        Long jogoId = ficha.getJogo().getId();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtualId, RoleJogo.MESTRE);

        if (isMestre) {
            return;
        }

        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: fichas de NPC só são acessíveis pelo Mestre.");
        }

        if (!usuarioAtualId.equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
        }
    }
}
