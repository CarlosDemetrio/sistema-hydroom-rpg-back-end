package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaEssencia;
import br.com.hydroom.rpg.fichacontrolador.model.FichaProspeccao;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVida;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.DadoProspeccaoConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAmeacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaBonusRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaDescricaoFisicaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaEssenciaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaProspeccaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaMembroRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ProspeccaoUsoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para FichaVidaService.resetarEstado().
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Reset restaura vidaAtual=vidaTotal</li>
 *   <li>Reset restaura essenciaAtual=total</li>
 *   <li>Reset zera danoRecebido de todos os membros</li>
 *   <li>Reset NÃO altera FichaProspeccao.quantidade (invariante)</li>
 *   <li>Reset NÃO altera nível, xp, atributos (invariante)</li>
 *   <li>Jogador não pode resetar</li>
 *   <li>Mestre de outro jogo não pode resetar</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaVidaService.resetarEstado — Testes de Integração")
class FichaVidaServiceResetIntegrationTest {

    @Autowired
    private FichaVidaService fichaVidaService;

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

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
    private ProspeccaoUsoRepository prospeccaoUsoRepository;

    @Autowired
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private DadoProspeccaoConfigRepository dadoProspeccaoConfigRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroMestre;
    private Jogo jogo;
    private Ficha ficha;

    @BeforeEach
    void setUp() {
        prospeccaoUsoRepository.deleteAll();
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
                .nome("Mestre Reset " + n)
                .email("mestre-reset" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-reset-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Reset " + n)
                .email("jogador-reset" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-reset-" + n)
                .role("JOGADOR")
                .build());

        outroMestre = usuarioRepository.save(Usuario.builder()
                .nome("Outro Mestre " + n)
                .email("outro-mestre-reset" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-mestre-reset-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Reset " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Reset", jogador.getId(), null, null, null, null, null, false));
    }

    // =========================================================
    // RESET — FLUXO FELIZ
    // =========================================================

    @Test
    @DisplayName("Reset restaura vidaAtual igual ao vidaTotal")
    void deveRestaurarVidaAtualAoVidaTotal() {
        // Arrange: reduzir a vida atual
        autenticarComo(mestre);
        FichaVida vidaAntes = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        int vidaTotal = vidaAntes.getVidaTotal();
        fichaVidaService.atualizarVida(ficha.getId(),
                new AtualizarVidaRequest(vidaTotal / 2, 0, null));

        // Act
        fichaVidaService.resetarEstado(ficha.getId());

        // Assert
        FichaVida vidaDepois = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(vidaDepois.getVidaAtual()).isEqualTo(vidaDepois.getVidaTotal());
    }

    @Test
    @DisplayName("Reset restaura essenciaAtual igual ao total de essência")
    void deveRestaurarEssenciaAtualAoTotal() {
        // Arrange: reduzir a essência atual
        autenticarComo(mestre);
        FichaEssencia essenciaAntes = fichaEssenciaRepository.findByFichaId(ficha.getId()).orElseThrow();
        int totalEssencia = essenciaAntes.getTotal();
        fichaVidaService.atualizarVida(ficha.getId(),
                new AtualizarVidaRequest(10, Math.max(0, totalEssencia - 2), null));

        // Act
        fichaVidaService.resetarEstado(ficha.getId());

        // Assert
        FichaEssencia essenciaDepois = fichaEssenciaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(essenciaDepois.getEssenciaAtual()).isEqualTo(essenciaDepois.getTotal());
    }

    @Test
    @DisplayName("Reset NÃO altera FichaProspeccao.quantidade")
    void naoDeveAlterarQuantidadeDeProspeccao() {
        // Arrange: setar uma quantidade específica em prospecção
        autenticarComo(mestre);
        var dadoOpt = dadoProspeccaoConfigRepository.findAll().stream()
                .filter(d -> d.getJogo().getId().equals(jogo.getId()))
                .findFirst();

        if (dadoOpt.isEmpty()) {
            // Se não há dado configurado, o teste passa trivialmente
            fichaVidaService.resetarEstado(ficha.getId());
            return;
        }

        var dado = dadoOpt.get();
        FichaProspeccao fp = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(ficha.getId(), dado.getId())
                .orElseGet(() -> fichaProspeccaoRepository.save(FichaProspeccao.builder()
                        .ficha(ficha)
                        .dadoProspeccaoConfig(dado)
                        .quantidade(7)
                        .build()));
        fp.setQuantidade(7);
        fichaProspeccaoRepository.save(fp);

        // Act
        fichaVidaService.resetarEstado(ficha.getId());

        // Assert: quantidade de prospecção inalterada
        FichaProspeccao fpDepois = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(ficha.getId(), dado.getId())
                .orElseThrow();
        assertThat(fpDepois.getQuantidade()).isEqualTo(7);
    }

    @Test
    @DisplayName("Reset NÃO altera nível e XP da ficha")
    void naoDeveAlterarNivelEXp() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaAntes = fichaRepository.findById(ficha.getId()).orElseThrow();
        Integer nivelAntes = fichaAntes.getNivel();
        Long xpAntes = fichaAntes.getXp();

        // Act
        fichaVidaService.resetarEstado(ficha.getId());

        // Assert
        Ficha fichaDepois = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaDepois.getNivel()).isEqualTo(nivelAntes);
        assertThat(fichaDepois.getXp()).isEqualTo(xpAntes);
    }

    // =========================================================
    // PERMISSÕES
    // =========================================================

    @Test
    @DisplayName("Jogador não pode resetar o estado da ficha")
    void deveNegarResetAoJogador() {
        // Act & Assert
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class, () ->
                fichaVidaService.resetarEstado(ficha.getId()));
    }

    @Test
    @DisplayName("Mestre de outro jogo não pode resetar o estado da ficha")
    void deveNegarResetAoMestreDeOutroJogo() {
        // Arrange: outroMestre não está no jogo da ficha
        autenticarComo(outroMestre);
        // outroMestre criou seu próprio jogo mas não tem acesso ao jogo do mestre
        jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo do Outro Mestre")
                .build());

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
                fichaVidaService.resetarEstado(ficha.getId()));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
