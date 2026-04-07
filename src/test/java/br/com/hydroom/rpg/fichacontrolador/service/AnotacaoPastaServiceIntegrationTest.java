package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAnotacaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarPastaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarAnotacaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarPastaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.AnotacaoPastaResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoAnotacao;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
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
 * Testes de integração para AnotacaoPastaService.
 *
 * <p>Cobre regras hierárquicas e de acesso:</p>
 * <ul>
 *   <li>Criação de pastas raiz, nível 2 e nível 3</li>
 *   <li>Bloqueio de criação além do nível 3</li>
 *   <li>Validação de nome duplicado no mesmo pai</li>
 *   <li>Deleção com desaninhamento de sub-pastas</li>
 *   <li>Listagem em árvore hierárquica</li>
 *   <li>Controle de acesso (Jogador em ficha alheia, Mestre em qualquer ficha)</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("AnotacaoPastaService - Testes de Integração")
class AnotacaoPastaServiceIntegrationTest {

    @Autowired
    private AnotacaoPastaService anotacaoPastaService;

    @Autowired
    private FichaAnotacaoService fichaAnotacaoService;

    @Autowired
    private AnotacaoPastaRepository anotacaoPastaRepository;

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
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private FichaAnotacaoRepository fichaAnotacaoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Jogo jogo;
    private Ficha fichaDoJogador;
    private Ficha fichaDeNpc;

    @BeforeEach
    void setUp() {
        anotacaoPastaRepository.deleteAll();
        fichaAnotacaoRepository.deleteAll();
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
                .nome("Mestre Pasta")
                .email("mestre.pasta" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-pasta-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Pasta")
                .email("jogador.pasta" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-pasta-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Campanha Pasta " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        fichaDoJogador = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem do Jogador", jogador.getId(),
                null, null, null, null, null, false));

        fichaDeNpc = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "NPC do Mestre", null,
                null, null, null, null, null, true));
    }

    // =========================================================
    // TESTES DE CRIAÇÃO
    // =========================================================

    @Test
    @DisplayName("Deve criar pasta raiz (sem pai) com sucesso")
    void deveCriarPastaRaiz() {
        // Arrange
        autenticarComo(jogador);
        var request = new CriarPastaRequest("Missões", null, 0);

        // Act
        AnotacaoPastaResponse response = anotacaoPastaService.criar(
                fichaDoJogador.getId(), request, jogador.getId());

        // Assert
        assertThat(response.id()).isNotNull();
        assertThat(response.nome()).isEqualTo("Missões");
        assertThat(response.pastaPaiId()).isNull();
        assertThat(response.fichaId()).isEqualTo(fichaDoJogador.getId());
        assertThat(response.ordemExibicao()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve criar sub-pasta de nível 2 (filha de raiz) com sucesso")
    void deveCriarSubPastaDeNivel2() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse raiz = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Missões", null, 0),
                jogador.getId());

        // Act
        AnotacaoPastaResponse nivel2 = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Missões Ativas", raiz.id(), 0),
                jogador.getId());

        // Assert
        assertThat(nivel2.id()).isNotNull();
        assertThat(nivel2.nome()).isEqualTo("Missões Ativas");
        assertThat(nivel2.pastaPaiId()).isEqualTo(raiz.id());
    }

    @Test
    @DisplayName("Deve criar sub-pasta de nível 3 (neto de raiz) com sucesso")
    void deveCriarSubPastaDeNivel3() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse nivel1 = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Missões", null, 0),
                jogador.getId());

        AnotacaoPastaResponse nivel2 = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Missões Ativas", nivel1.id(), 0),
                jogador.getId());

        // Act
        AnotacaoPastaResponse nivel3 = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Missões Urgentes", nivel2.id(), 0),
                jogador.getId());

        // Assert
        assertThat(nivel3.id()).isNotNull();
        assertThat(nivel3.nome()).isEqualTo("Missões Urgentes");
        assertThat(nivel3.pastaPaiId()).isEqualTo(nivel2.id());
    }

    @Test
    @DisplayName("Deve impedir criação de nível 4 (pai no nível 3) lançando BusinessException")
    void deveImpedirCriacaoDeNivel4() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse nivel1 = anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("N1", null, 0), jogador.getId());

        AnotacaoPastaResponse nivel2 = anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("N2", nivel1.id(), 0), jogador.getId());

        AnotacaoPastaResponse nivel3 = anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("N3", nivel2.id(), 0), jogador.getId());

        // Act & Assert
        assertThrows(BusinessException.class, () ->
                anotacaoPastaService.criar(
                        fichaDoJogador.getId(),
                        new CriarPastaRequest("N4 Bloqueado", nivel3.id(), 0),
                        jogador.getId())
        );
    }

    @Test
    @DisplayName("Deve impedir nome duplicado no mesmo pai lançando ConflictException")
    void deveImpedirNomeDuplicadoNoPai() {
        // Arrange
        autenticarComo(jogador);
        anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Missões", null, 0),
                jogador.getId());

        // Act & Assert
        assertThrows(ConflictException.class, () ->
                anotacaoPastaService.criar(
                        fichaDoJogador.getId(),
                        new CriarPastaRequest("Missões", null, 1),
                        jogador.getId())
        );
    }

    // =========================================================
    // TESTES DE DELEÇÃO
    // =========================================================

    @Test
    @DisplayName("Deve deletar pasta pai e desaninhar sub-pastas para a raiz")
    void deveDeletarPastaDesaninhando() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse pai = anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("Pai", null, 0), jogador.getId());

        AnotacaoPastaResponse filho1 = anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("Filho 1", pai.id(), 0), jogador.getId());

        AnotacaoPastaResponse filho2 = anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("Filho 2", pai.id(), 1), jogador.getId());

        // Act - deletar pasta pai
        anotacaoPastaService.deletar(fichaDoJogador.getId(), pai.id(), jogador.getId());

        // Assert - sub-pastas existem e foram movidas para raiz
        List<AnotacaoPastaResponse> arvore = anotacaoPastaService.listarArvore(
                fichaDoJogador.getId(), jogador.getId());

        assertThat(arvore).hasSize(2);
        assertThat(arvore).extracting(AnotacaoPastaResponse::nome)
                .containsExactlyInAnyOrder("Filho 1", "Filho 2");
        assertThat(arvore).allMatch(p -> p.pastaPaiId() == null);
    }

    // =========================================================
    // TESTES DE LISTAGEM
    // =========================================================

    @Test
    @DisplayName("Deve listar árvore completa com sub-pastas aninhadas corretamente")
    void deveListarArvoreCompleta() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse raiz = anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("Raiz", null, 0), jogador.getId());

        AnotacaoPastaResponse filhoA = anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("Filho A", raiz.id(), 0), jogador.getId());

        anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("Neto AA", filhoA.id(), 0), jogador.getId());

        anotacaoPastaService.criar(
                fichaDoJogador.getId(), new CriarPastaRequest("Filho B", raiz.id(), 1), jogador.getId());

        // Act
        List<AnotacaoPastaResponse> arvore = anotacaoPastaService.listarArvore(
                fichaDoJogador.getId(), jogador.getId());

        // Assert - estrutura hierárquica correta
        assertThat(arvore).hasSize(1);
        AnotacaoPastaResponse raizResponse = arvore.get(0);
        assertThat(raizResponse.nome()).isEqualTo("Raiz");
        assertThat(raizResponse.subPastas()).hasSize(2);

        AnotacaoPastaResponse filhoAResponse = raizResponse.subPastas().stream()
                .filter(p -> "Filho A".equals(p.nome()))
                .findFirst()
                .orElseThrow();

        assertThat(filhoAResponse.subPastas()).hasSize(1);
        assertThat(filhoAResponse.subPastas().get(0).nome()).isEqualTo("Neto AA");
    }

    // =========================================================
    // TESTES DE CONTROLE DE ACESSO
    // =========================================================

    @Test
    @DisplayName("Jogador deve ser impedido de operar em pasta de ficha alheia lançando ForbiddenException")
    void deveImpedirJogadorEmFichaAlheia() {
        // Arrange - criar outro jogador com sua ficha
        int n = counter.getAndIncrement();
        Usuario outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Jogador")
                .email("outro" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-" + n)
                .role("JOGADOR")
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(outroJogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        autenticarComo(mestre);
        Ficha fichaDoOutro = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem do Outro", outroJogador.getId(),
                null, null, null, null, null, false));

        // Act - jogador tenta criar pasta em ficha alheia
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class, () ->
                anotacaoPastaService.criar(
                        fichaDoOutro.getId(),
                        new CriarPastaRequest("Pasta Invasiva", null, 0),
                        jogador.getId())
        );
    }

    @Test
    @DisplayName("Mestre deve poder criar pasta em ficha de NPC")
    void devePermitirMestreEmQualquerFicha() {
        // Arrange
        autenticarComo(mestre);
        var request = new CriarPastaRequest("Pasta do NPC", null, 0);

        // Act
        AnotacaoPastaResponse response = anotacaoPastaService.criar(
                fichaDeNpc.getId(), request, mestre.getId());

        // Assert
        assertThat(response.id()).isNotNull();
        assertThat(response.nome()).isEqualTo("Pasta do NPC");
        assertThat(response.fichaId()).isEqualTo(fichaDeNpc.getId());
    }

    @Test
    @DisplayName("Jogador deve ser impedido de criar pasta em ficha de NPC lançando ForbiddenException")
    void deveImpedirJogadorEmFichaDeNpc() {
        // Arrange
        autenticarComo(jogador);

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
                anotacaoPastaService.criar(
                        fichaDeNpc.getId(),
                        new CriarPastaRequest("Tentativa Indevida", null, 0),
                        jogador.getId())
        );
    }

    // =========================================================
    // TESTES DE INTEGRAÇÃO COM ANOTAÇÕES (Spec 011 T4)
    // =========================================================

    @Test
    @DisplayName("Deve criar anotação vinculada a uma pasta existente")
    void deveCriarAnotacaoNaPasta() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse pasta = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Missões", null, 0),
                jogador.getId());

        var request = new CriarAnotacaoRequest(
                "Objetivo principal",
                "Investigar a torre norte",
                TipoAnotacao.JOGADOR,
                false,
                pasta.id(),
                false);

        // Act
        FichaAnotacao anotacao = fichaAnotacaoService.criar(
                fichaDoJogador.getId(), request, jogador.getId());

        // Assert
        assertThat(anotacao.getId()).isNotNull();
        assertThat(anotacao.getPastaPai()).isNotNull();
        assertThat(anotacao.getPastaPai().getId()).isEqualTo(pasta.id());
    }

    @Test
    @DisplayName("Deve mover anotação de uma pasta para outra")
    void deveMoverAnotacaoEntrePastas() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse pasta1 = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Pasta Origem", null, 0),
                jogador.getId());

        AnotacaoPastaResponse pasta2 = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Pasta Destino", null, 1),
                jogador.getId());

        FichaAnotacao anotacao = fichaAnotacaoService.criar(
                fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota movível", "Conteúdo", TipoAnotacao.JOGADOR, false, pasta1.id(), false),
                jogador.getId());

        assertThat(anotacao.getPastaPai().getId()).isEqualTo(pasta1.id());

        // Act
        FichaAnotacao anotacaoAtualizada = fichaAnotacaoService.atualizar(
                fichaDoJogador.getId(),
                anotacao.getId(),
                new AtualizarAnotacaoRequest(null, null, null, null, pasta2.id()),
                jogador.getId());

        // Assert
        assertThat(anotacaoAtualizada.getPastaPai()).isNotNull();
        assertThat(anotacaoAtualizada.getPastaPai().getId()).isEqualTo(pasta2.id());
    }

    @Test
    @DisplayName("Deve listar apenas as anotações da pasta informada")
    void deveListarAnotacoesFiltrandoPorPasta() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse pasta = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Missões", null, 0),
                jogador.getId());

        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota na pasta 1", "Conteúdo", TipoAnotacao.JOGADOR, false, pasta.id(), false),
                jogador.getId());

        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota na pasta 2", "Conteúdo", TipoAnotacao.JOGADOR, false, pasta.id(), false),
                jogador.getId());

        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota raiz", "Sem pasta", TipoAnotacao.JOGADOR, false, null, false),
                jogador.getId());

        // Act — filtrar por pastaPaiId: apenas anotações dentro da pasta
        autenticarComo(mestre);
        List<FichaAnotacao> anotacoesDaPasta = fichaAnotacaoService.listar(
                fichaDoJogador.getId(), pasta.id());

        // Assert
        assertThat(anotacoesDaPasta).hasSize(2);
        assertThat(anotacoesDaPasta).allMatch(a -> a.getPastaPai() != null
                && pasta.id().equals(a.getPastaPai().getId()));
    }

    @Test
    @DisplayName("Deve retornar todas as anotações quando pastaPaiId é nulo (sem filtro de pasta)")
    void deveListarAnotacoesRaiz() {
        // Arrange
        autenticarComo(jogador);
        AnotacaoPastaResponse pasta = anotacaoPastaService.criar(
                fichaDoJogador.getId(),
                new CriarPastaRequest("Pasta Qualquer", null, 0),
                jogador.getId());

        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota raiz 1", "Conteúdo", TipoAnotacao.JOGADOR, false, null, false),
                jogador.getId());

        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota raiz 2", "Conteúdo", TipoAnotacao.JOGADOR, false, null, false),
                jogador.getId());

        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota na pasta", "Conteúdo", TipoAnotacao.JOGADOR, false, pasta.id(), false),
                jogador.getId());

        // Act — pastaPaiId=null: comportamento atual do service retorna todas as anotações
        // (sem filtro de pasta aplicado ao resultado; filtragem de raiz requer implementação futura)
        autenticarComo(mestre);
        List<FichaAnotacao> todasAnotacoes = fichaAnotacaoService.listar(
                fichaDoJogador.getId(), null);

        // Assert — retorna todas as 3 anotações (raiz e com pasta), pois null = sem filtro
        assertThat(todasAnotacoes).hasSize(3);
        assertThat(todasAnotacoes).extracting(FichaAnotacao::getTitulo)
                .containsExactlyInAnyOrder("Nota raiz 1", "Nota raiz 2", "Nota na pasta");
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
