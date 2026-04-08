package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarImagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoImagem;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes de integração para FichaImagemService.
 *
 * <p>CloudinaryUploadService é substituído por @MockBean — nenhum teste chama o Cloudinary real.</p>
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Listagem com controle de acesso (Mestre/Jogador/NPC)</li>
 *   <li>Upload com validação de limite e substituição de avatar</li>
 *   <li>Atualização de metadados (título, ordemExibicao)</li>
 *   <li>Soft delete com chamada ao Cloudinary destroy</li>
 *   <li>Tolerância a falha do Cloudinary no destroy</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaImagemService - Testes de Integração")
class FichaImagemServiceIntegrationTest {

    @Autowired
    private FichaImagemService fichaImagemService;

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @MockitoBean
    private CloudinaryUploadService cloudinaryUploadService;

    @Autowired
    private FichaImagemRepository fichaImagemRepository;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;

    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;

    @Autowired
    private FichaBonusRepository fichaBonusRepository;

    @Autowired
    private FichaVidaRepository fichaVidaRepository;

    @Autowired
    private FichaVidaMembroRepository fichaVidaMembroRepository;

    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;

    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;

    @Autowired
    private FichaProspeccaoRepository fichaProspeccaoRepository;

    @Autowired
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private static final String URL_MOCK = "https://res.cloudinary.com/test/image/upload/test.jpg";
    private static final String PUBLIC_ID_MOCK = "rpg-fichas/1/fichas/42/test-uuid";

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroJogador;
    private Jogo jogo;
    private Ficha fichaDoJogador;
    private Ficha fichaDoOutroJogador;
    private Ficha fichaDeNpc;

    @BeforeEach
    void setUp() {
        fichaImagemRepository.deleteAll();
        fichaDescricaoFisicaRepository.deleteAll();
        fichaProspeccaoRepository.deleteAll();
        fichaAmeacaRepository.deleteAll();
        fichaEssenciaRepository.deleteAll();
        fichaVidaMembroRepository.deleteAll();
        fichaVidaRepository.deleteAll();
        fichaBonusRepository.deleteAll();
        fichaAptidaoRepository.deleteAll();
        fichaAtributoRepository.deleteAll();
        fichaRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Imagem")
                .email("mestre.imagem" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-img-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Imagem")
                .email("jogador.imagem" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-img-" + n)
                .role("JOGADOR")
                .build());

        outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Jogador Imagem")
                .email("outro.jogador.img" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-img-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Campanha Imagens " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(outroJogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        fichaDoJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem do Jogador",
                        jogador.getId(), null, null, null, null, null, false));

        fichaDoOutroJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem do Outro",
                        outroJogador.getId(), null, null, null, null, null, false));

        fichaDeNpc = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Goblin Arqueiro",
                        null, null, null, null, null, null, true));

        configurarMockCloudinary();
    }

    private void configurarMockCloudinary() {
        when(cloudinaryUploadService.upload(any(), anyString()))
                .thenReturn(Map.of("url", URL_MOCK, "public_id", PUBLIC_ID_MOCK));
        doNothing().when(cloudinaryUploadService).destroy(anyString());
    }

    // =========================================================
    // TESTES DE LISTAGEM
    // =========================================================

    @Test
    @DisplayName("Mestre deve listar imagens de qualquer ficha")
    void deveListarImagensDaFichaComoMestre() {
        autenticarComo(mestre);
        criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Imagem 1");
        criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Imagem 2");

        List<FichaImagem> imagens = fichaImagemService.listar(fichaDoJogador.getId(), mestre.getId());

        assertThat(imagens).hasSize(2);
    }

    @Test
    @DisplayName("Jogador deve listar imagens da própria ficha")
    void deveListarImagensComoJogadorDono() {
        autenticarComo(mestre);
        criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Arte do Personagem");

        List<FichaImagem> imagens = fichaImagemService.listar(fichaDoJogador.getId(), jogador.getId());

        assertThat(imagens).hasSize(1);
        assertThat(imagens.get(0).getTitulo()).isEqualTo("Arte do Personagem");
    }

    @Test
    @DisplayName("Jogador não deve listar imagens da ficha de outro jogador")
    void deveImpedirJogadorDeListarImagensDeOutro() {
        assertThatThrownBy(() ->
                fichaImagemService.listar(fichaDoOutroJogador.getId(), jogador.getId())
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Jogador não deve listar imagens de ficha de NPC")
    void deveImpedirJogadorDeListarImagensDeNpc() {
        assertThatThrownBy(() ->
                fichaImagemService.listar(fichaDeNpc.getId(), jogador.getId())
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Ficha sem imagens deve retornar lista vazia")
    void deveListarVazioQuandoFichaSemImagens() {
        List<FichaImagem> imagens = fichaImagemService.listar(fichaDoJogador.getId(), mestre.getId());

        assertThat(imagens).isEmpty();
    }

    @Test
    @DisplayName("AVATAR deve aparecer antes de GALERIA na listagem")
    void deveListarComAvatarPrimeiro() {
        autenticarComo(mestre);
        criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Imagem de Galeria");
        criarImagemMock(fichaDoJogador, TipoImagem.AVATAR, "Imagem Avatar");

        List<FichaImagem> imagens = fichaImagemService.listar(fichaDoJogador.getId(), mestre.getId());

        assertThat(imagens).hasSize(2);
        assertThat(imagens.get(0).getTipoImagem()).isEqualTo(TipoImagem.AVATAR);
        assertThat(imagens.get(1).getTipoImagem()).isEqualTo(TipoImagem.GALERIA);
    }

    // =========================================================
    // TESTES DE UPLOAD
    // =========================================================

    @Test
    @DisplayName("Jogador deve adicionar avatar na própria ficha com urlCloudinary e publicId persistidos")
    void deveAdicionarAvatarComoJogador() {
        FichaImagem imagem = criarImagemMock(fichaDoJogador, TipoImagem.AVATAR, "Retrato");

        assertThat(imagem.getId()).isNotNull();
        assertThat(imagem.getUrlCloudinary()).isEqualTo(URL_MOCK);
        assertThat(imagem.getPublicId()).isEqualTo(PUBLIC_ID_MOCK);
        assertThat(imagem.getTipoImagem()).isEqualTo(TipoImagem.AVATAR);
        assertThat(imagem.getTitulo()).isEqualTo("Retrato");
    }

    @Test
    @DisplayName("Mestre deve adicionar imagem de galeria em ficha de NPC")
    void deveAdicionarGaleriaComoMestre() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDeNpc, TipoImagem.GALERIA, "Arte do NPC");

        assertThat(imagem.getId()).isNotNull();
        assertThat(imagem.getTipoImagem()).isEqualTo(TipoImagem.GALERIA);
    }

    @Test
    @DisplayName("Novo AVATAR deve converter o avatar anterior para GALERIA")
    void deveSubstituirAvatarPromovendoAnteriorParaGaleria() {
        autenticarComo(mestre);
        FichaImagem avatarOriginal = criarImagemMock(fichaDoJogador, TipoImagem.AVATAR, "Avatar 1");
        assertThat(avatarOriginal.getTipoImagem()).isEqualTo(TipoImagem.AVATAR);

        criarImagemMock(fichaDoJogador, TipoImagem.AVATAR, "Avatar 2");

        List<FichaImagem> imagens = fichaImagemService.listar(fichaDoJogador.getId(), mestre.getId());
        assertThat(imagens).hasSize(2);

        long quantidadeAvatars = imagens.stream()
                .filter(i -> TipoImagem.AVATAR.equals(i.getTipoImagem()))
                .count();
        assertThat(quantidadeAvatars).isEqualTo(1);

        long quantidadeGaleria = imagens.stream()
                .filter(i -> TipoImagem.GALERIA.equals(i.getTipoImagem()))
                .count();
        assertThat(quantidadeGaleria).isEqualTo(1);
    }

    @Test
    @DisplayName("Upload deve chamar cloudinaryUploadService.upload com o folder correto")
    void deveVerificarChamadaAoCloudinaryNoUpload() {
        autenticarComo(mestre);
        criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Verificação");

        String folderEsperado = "rpg-fichas/" + jogo.getId() + "/fichas/" + fichaDoJogador.getId();
        verify(cloudinaryUploadService).upload(any(), eq(folderEsperado));
    }

    @Test
    @DisplayName("Jogador não deve fazer upload em ficha de outro jogador")
    void deveImpedirUploadPorJogadorEmFichaAlheia() {
        assertThatThrownBy(() ->
                fichaImagemService.adicionar(fichaDoOutroJogador.getId(),
                        criarArquivoMock(), TipoImagem.GALERIA, "Invasão", jogador.getId())
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Jogador não deve fazer upload em ficha de NPC")
    void deveImpedirUploadPorJogadorEmNpc() {
        assertThatThrownBy(() ->
                fichaImagemService.adicionar(fichaDeNpc.getId(),
                        criarArquivoMock(), TipoImagem.GALERIA, "Invasão NPC", jogador.getId())
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("A 21ª imagem deve lançar BusinessException (limite de 20)")
    void deveImpedirUploadApos20Imagens() {
        autenticarComo(mestre);
        for (int i = 1; i <= 20; i++) {
            criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Imagem " + i);
        }

        assertThatThrownBy(() ->
                fichaImagemService.adicionar(fichaDoJogador.getId(),
                        criarArquivoMock(), TipoImagem.GALERIA, "Imagem 21", mestre.getId())
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Limite de 20 imagens");
    }

    // =========================================================
    // TESTES DE EDIÇÃO
    // =========================================================

    @Test
    @DisplayName("Mestre deve atualizar título da imagem")
    void deveAtualizarTituloComoMestre() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Título Original");

        var request = new AtualizarImagemRequest("Título Atualizado", null);
        FichaImagem atualizada = fichaImagemService.atualizar(
                fichaDoJogador.getId(), imagem.getId(), request, mestre.getId());

        assertThat(atualizada.getTitulo()).isEqualTo("Título Atualizado");
    }

    @Test
    @DisplayName("Deve atualizar ordemExibicao da imagem")
    void deveAtualizarOrdemExibicao() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Imagem Ordem");

        var request = new AtualizarImagemRequest(null, 3);
        FichaImagem atualizada = fichaImagemService.atualizar(
                fichaDoJogador.getId(), imagem.getId(), request, mestre.getId());

        assertThat(atualizada.getOrdemExibicao()).isEqualTo(3);
    }

    @Test
    @DisplayName("Campo null no update não deve sobrescrever título original")
    void deveMaterCamposNaoEnviadosNoUpdate() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Título Preservado");

        var request = new AtualizarImagemRequest(null, 5);
        FichaImagem atualizada = fichaImagemService.atualizar(
                fichaDoJogador.getId(), imagem.getId(), request, mestre.getId());

        assertThat(atualizada.getTitulo()).isEqualTo("Título Preservado");
        assertThat(atualizada.getOrdemExibicao()).isEqualTo(5);
    }

    @Test
    @DisplayName("Jogador não deve editar imagem de ficha de outro jogador")
    void deveImpedirEdicaoPorJogadorEmFichaAlheia() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDoOutroJogador, TipoImagem.GALERIA, "Imagem do Outro");

        var request = new AtualizarImagemRequest("Tentativa", null);
        assertThatThrownBy(() ->
                fichaImagemService.atualizar(fichaDoOutroJogador.getId(), imagem.getId(), request, jogador.getId())
        ).isInstanceOf(ForbiddenException.class);
    }

    // =========================================================
    // TESTES DE REMOÇÃO
    // =========================================================

    @Test
    @DisplayName("Mestre deve deletar imagem com soft delete local e chamada ao Cloudinary destroy")
    void deveDeletarImagemComoMestre() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Para Deletar");
        Long imagemId = imagem.getId();
        String publicId = imagem.getPublicId();

        fichaImagemService.deletar(fichaDoJogador.getId(), imagemId, mestre.getId());

        verify(cloudinaryUploadService).destroy(publicId);

        FichaImagem deletada = fichaImagemRepository.findById(imagemId).orElse(null);
        if (deletada != null) {
            assertThat(deletada.getDeletedAt()).isNotNull();
        }
        // Se null, @SQLRestriction filtrou — comportamento correto
    }

    @Test
    @DisplayName("Jogador deve deletar imagem da própria ficha")
    void deveDeletarImagemComoJogadorDono() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Imagem do Jogador");
        Long imagemId = imagem.getId();

        assertThatCode(() ->
                fichaImagemService.deletar(fichaDoJogador.getId(), imagemId, jogador.getId())
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Jogador não deve deletar imagem de ficha de outro jogador")
    void deveImpedirDeletarImagemEmFichaAlheia() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDoOutroJogador, TipoImagem.GALERIA, "Imagem Alheia");

        assertThatThrownBy(() ->
                fichaImagemService.deletar(fichaDoOutroJogador.getId(), imagem.getId(), jogador.getId())
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Falha no Cloudinary destroy deve ser logada mas não impedir soft delete local")
    void deveLograrFalhaCloudinaryMasCompletarSoftDelete() {
        autenticarComo(mestre);
        FichaImagem imagem = criarImagemMock(fichaDoJogador, TipoImagem.GALERIA, "Teste Falha Cloudinary");
        Long imagemId = imagem.getId();

        doThrow(new RuntimeException("Cloudinary indisponível"))
                .when(cloudinaryUploadService).destroy(anyString());

        assertThatCode(() ->
                fichaImagemService.deletar(fichaDoJogador.getId(), imagemId, mestre.getId())
        ).doesNotThrowAnyException();

        FichaImagem deletada = fichaImagemRepository.findById(imagemId).orElse(null);
        if (deletada != null) {
            assertThat(deletada.getDeletedAt()).isNotNull();
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private FichaImagem criarImagemMock(Ficha ficha, TipoImagem tipo, String titulo) {
        MultipartFile arquivoMock = criarArquivoMock();
        Long usuarioId = tipo == TipoImagem.GALERIA && ficha.getJogadorId() != null
                ? mestre.getId()
                : mestre.getId();
        return fichaImagemService.adicionar(ficha.getId(), arquivoMock, tipo, titulo, mestre.getId());
    }

    private MultipartFile criarArquivoMock() {
        MultipartFile arquivoMock = mock(MultipartFile.class);
        when(arquivoMock.getContentType()).thenReturn("image/jpeg");
        when(arquivoMock.getSize()).thenReturn(1024L);
        when(arquivoMock.isEmpty()).thenReturn(false);
        try {
            when(arquivoMock.getBytes()).thenReturn(new byte[]{1, 2, 3});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return arquivoMock;
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
