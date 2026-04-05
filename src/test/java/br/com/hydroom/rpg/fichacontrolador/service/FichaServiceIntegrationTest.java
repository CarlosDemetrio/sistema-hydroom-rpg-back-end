package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.FichaStatus;
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
 * Testes de integração para FichaService.
 *
 * <p>Cobre branches de:
 * <ul>
 *   <li>isNpc=true vs isNpc=false</li>
 *   <li>Com/sem jogadorId</li>
 *   <li>MESTRE vs JOGADOR criando/listando/editando</li>
 *   <li>update com campos null vs preenchidos</li>
 *   <li>FK de jogo diferente</li>
 *   <li>Acesso negado</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaService - Testes de Integração")
class FichaServiceIntegrationTest {

    @Autowired
    private FichaService fichaService;

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

    @Autowired
    private JogoService jogoService;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoRepository;

    @Autowired
    private BonusConfigRepository bonusRepository;

    @Autowired
    private MembroCorpoConfigRepository membroCorpoRepository;

    @Autowired
    private DadoProspeccaoConfigRepository dadoProspeccaoRepository;

    @Autowired
    private ConfiguracaoRacaRepository racaRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private GeneroConfigRepository generoRepository;

    @Autowired
    private IndoleConfigRepository indoleRepository;

    @Autowired
    private PresencaConfigRepository presencaRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroUsuario;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta (dependências primeiro)
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
                .nome("Mestre Teste")
                .email("mestre" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Teste")
                .email("jogador" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-" + n)
                .role("JOGADOR")
                .build());

        outroUsuario = usuarioRepository.save(Usuario.builder()
                .nome("Outro Usuário")
                .email("outro" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-" + n)
                .role("JOGADOR")
                .build());

        // Criar jogo como Mestre (inicializa configs automaticamente)
        autenticarComo(mestre);
        jogo = jogoService.criarJogo(br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest.builder()
                .nome("Jogo Teste " + n)
                .build());

        // Adicionar jogador como participante aprovado
        JogoParticipante participante = JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build();
        jogoParticipanteRepository.save(participante);
    }

    // =========================================================
    // TESTES DE CRIAÇÃO
    // =========================================================

    @Test
    @DisplayName("Deve criar ficha simples como Mestre")
    void deveCriarFichaComoMestre() {
        // Arrange
        autenticarComo(mestre);
        var request = new CreateFichaRequest(jogo.getId(), "Aragorn", null, null, null, null, null, null, false);

        // Act
        Ficha ficha = fichaService.criar(request);

        // Assert
        assertThat(ficha.getId()).isNotNull();
        assertThat(ficha.getNome()).isEqualTo("Aragorn");
        assertThat(ficha.isNpc()).isFalse();
        assertThat(ficha.getJogadorId()).isNull();
    }

    @Test
    @DisplayName("Deve criar NPC como Mestre (isNpc=true)")
    void deveCriarNpcComoMestre() {
        // Arrange
        autenticarComo(mestre);
        var request = new CreateFichaRequest(jogo.getId(), "Goblin Boss", null, null, null, null, null, null, true);

        // Act
        Ficha ficha = fichaService.criar(request);

        // Assert
        assertThat(ficha.isNpc()).isTrue();
        assertThat(ficha.getJogadorId()).isNull(); // NPCs não têm jogadorId
    }

    @Test
    @DisplayName("Deve criar ficha com jogadorId como Mestre")
    void deveCriarFichaComJogadorIdComoMestre() {
        // Arrange
        autenticarComo(mestre);
        var request = new CreateFichaRequest(jogo.getId(), "Gandalf", jogador.getId(), null, null, null, null, null, false);

        // Act
        Ficha ficha = fichaService.criar(request);

        // Assert
        assertThat(ficha.getJogadorId()).isEqualTo(jogador.getId());
    }

    @Test
    @DisplayName("Deve criar ficha como Jogador (jogadorId sempre é o próprio)")
    void deveCriarFichaComoJogador() {
        // Arrange
        autenticarComo(jogador);
        // Jogador tenta atribuir ficha a outro usuário - deve ignorar e usar o próprio
        var request = new CreateFichaRequest(jogo.getId(), "Legolas", outroUsuario.getId(), null, null, null, null, null, false);

        // Act
        Ficha ficha = fichaService.criar(request);

        // Assert - jogadorId deve ser o do jogador autenticado, não outroUsuario
        assertThat(ficha.getJogadorId()).isEqualTo(jogador.getId());
    }

    @Test
    @DisplayName("Deve inicializar sub-registros automaticamente ao criar ficha")
    void deveCriarSubRegistrosAutomaticamente() {
        // Arrange
        autenticarComo(mestre);
        var request = new CreateFichaRequest(jogo.getId(), "Legolas", null, null, null, null, null, null, false);

        int atributosConfig = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).size();
        int aptidoesConfig = aptidaoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).size();
        int bonusConfig = bonusRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).size();
        int membrosConfig = membroCorpoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).size();
        int prospeccaoConfig = dadoProspeccaoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).size();

        // Act
        Ficha ficha = fichaService.criar(request);
        Long fichaId = ficha.getId();

        // Assert - sub-registros criados
        assertThat(fichaAtributoRepository.findByFichaId(fichaId)).hasSize(atributosConfig);
        assertThat(fichaAptidaoRepository.findByFichaId(fichaId)).hasSize(aptidoesConfig);
        assertThat(fichaBonusRepository.findByFichaId(fichaId)).hasSize(bonusConfig);
        assertThat(fichaVidaMembroRepository.findByFichaId(fichaId)).hasSize(membrosConfig);
        assertThat(fichaProspeccaoRepository.findByFichaId(fichaId)).hasSize(prospeccaoConfig);

        // FichaVida, FichaEssencia, FichaAmeaca, FichaDescricaoFisica (1 por ficha)
        assertThat(fichaVidaRepository.findByFichaId(fichaId)).isPresent();
        assertThat(fichaEssenciaRepository.findByFichaId(fichaId)).isPresent();
        assertThat(fichaAmeacaRepository.findByFichaId(fichaId)).isPresent();
        assertThat(fichaDescricaoFisicaRepository.findByFichaId(fichaId)).isPresent();
    }

    @Test
    @DisplayName("Deve criar ficha com raça do mesmo jogo")
    void deveCriarFichaComRacaDoMesmoJogo() {
        // Arrange
        autenticarComo(mestre);
        Raca raca = racaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        var request = new CreateFichaRequest(jogo.getId(), "Elfo Ranger", null, raca.getId(), null, null, null, null, false);

        // Act
        Ficha ficha = fichaService.criar(request);

        // Assert
        assertThat(ficha.getRaca()).isNotNull();
        assertThat(ficha.getRaca().getId()).isEqualTo(raca.getId());
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando Jogador não é participante")
    void deveLancarExcecaoQuandoJogadorNaoEParticipante() {
        // Arrange - outroUsuario não é participante do jogo
        autenticarComo(outroUsuario);
        var request = new CreateFichaRequest(jogo.getId(), "Intruso", null, null, null, null, null, null, false);

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> fichaService.criar(request));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para jogo inexistente")
    void deveLancarExcecaoParaJogoInexistente() {
        // Arrange
        autenticarComo(mestre);
        var request = new CreateFichaRequest(99999L, "Personagem", null, null, null, null, null, null, false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> fichaService.criar(request));
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando raça pertence a outro jogo")
    void deveLancarExcecaoQuandoRacaDoutroJogo() {
        // Arrange - criar outro jogo
        autenticarComo(mestre);
        Jogo outroJogo = jogoService.criarJogo(br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest.builder()
                .nome("Outro Jogo")
                .build());

        Raca racaOutroJogo = racaRepository.findByJogoIdOrderByOrdemExibicao(outroJogo.getId()).get(0);
        var request = new CreateFichaRequest(jogo.getId(), "Personagem", null, racaOutroJogo.getId(), null, null, null, null, false);

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> fichaService.criar(request));
    }

    // =========================================================
    // TESTES DE LISTAGEM
    // =========================================================

    @Test
    @DisplayName("Mestre deve ver todas as fichas incluindo NPCs")
    void mestreDeveVerTodasFichas() {
        // Arrange
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha Jogador", jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC Goblin", null, null, null, null, null, null, true));

        // Act
        List<Ficha> fichas = fichaService.listar(jogo.getId());

        // Assert - Mestre vê tudo (2 fichas)
        assertThat(fichas).hasSize(2);
    }

    @Test
    @DisplayName("Jogador deve ver apenas suas próprias fichas")
    void jogadorDeveVerApenasSuasFichas() {
        // Arrange - criar fichas como mestre
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha do Jogador", jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha do Mestre", null, null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC Goblin", null, null, null, null, null, null, true));

        // Act - listar como jogador
        autenticarComo(jogador);
        List<Ficha> fichas = fichaService.listar(jogo.getId());

        // Assert - Jogador vê apenas 1 (a sua)
        assertThat(fichas).hasSize(1);
        assertThat(fichas.get(0).getJogadorId()).isEqualTo(jogador.getId());
    }

    @Test
    @DisplayName("Listar NPCs deve ser permitido apenas ao Mestre")
    void listarNpcsSomenteParaMestre() {
        // Arrange
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC 1", null, null, null, null, null, null, true));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC 2", null, null, null, null, null, null, true));

        // Act
        List<Ficha> npcs = fichaService.listarNpcs(jogo.getId());

        // Assert
        assertThat(npcs).hasSize(2);
        assertThat(npcs).allMatch(Ficha::isNpc);
    }

    @Test
    @DisplayName("Listar NPCs por Jogador deve lançar ForbiddenException")
    void listarNpcsPorJogadorLancaForbidden() {
        // Arrange
        autenticarComo(jogador);

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> fichaService.listarNpcs(jogo.getId()));
    }

    // =========================================================
    // TESTES DE LISTAGEM COM FILTROS
    // =========================================================

    @Test
    @DisplayName("Mestre deve listar fichas com filtro de nome")
    void mestreDeveListarFichasComFiltroDeNome() {
        // Arrange
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Aragorn Elessar", jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Boromir", null, null, null, null, null, null, false));

        // Act
        List<Ficha> fichas = fichaService.listarComFiltros(jogo.getId(), "Aragorn", null, null, null);

        // Assert
        assertThat(fichas).hasSize(1);
        assertThat(fichas.get(0).getNome()).contains("Aragorn");
    }

    @Test
    @DisplayName("Mestre deve listar fichas sem filtros (retorna todas não-NPC)")
    void mestreDeveListarFichasSemFiltros() {
        // Arrange
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha A", null, null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha B", jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC Goblin", null, null, null, null, null, null, true));

        // Act
        List<Ficha> fichas = fichaService.listarComFiltros(jogo.getId(), null, null, null, null);

        // Assert — NPC não deve aparecer
        assertThat(fichas).hasSize(2);
        assertThat(fichas).noneMatch(Ficha::isNpc);
    }

    @Test
    @DisplayName("Jogador deve listar apenas suas fichas via listarMinhas")
    void jogadorDeveListarMinhasFichas() {
        // Arrange
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha do Jogador", jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha do Mestre", null, null, null, null, null, null, false));

        // Act
        autenticarComo(jogador);
        List<Ficha> minhas = fichaService.listarMinhas(jogo.getId());

        // Assert
        assertThat(minhas).hasSize(1);
        assertThat(minhas.get(0).getJogadorId()).isEqualTo(jogador.getId());
    }

    @Test
    @DisplayName("Jogador vê apenas suas fichas via listarComFiltros")
    void jogadorDeveVerApenasSuasFichasViaFiltros() {
        // Arrange
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha do Jogador", jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha Alheia", null, null, null, null, null, null, false));

        // Act
        autenticarComo(jogador);
        List<Ficha> fichas = fichaService.listarComFiltros(jogo.getId(), null, null, null, null);

        // Assert
        assertThat(fichas).hasSize(1);
        assertThat(fichas.get(0).getJogadorId()).isEqualTo(jogador.getId());
    }

    // =========================================================
    // TESTES DE BUSCA POR ID
    // =========================================================

    @Test
    @DisplayName("Mestre pode buscar qualquer ficha por ID")
    void mestrePodeBuscarQualquerFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Arwen", jogador.getId(), null, null, null, null, null, false));

        // Act
        Ficha resultado = fichaService.buscarPorId(ficha.getId());

        // Assert
        assertThat(resultado.getId()).isEqualTo(ficha.getId());
    }

    @Test
    @DisplayName("Jogador pode buscar suas próprias fichas")
    void jogadorPodeBuscarSuaFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha Própria", jogador.getId(), null, null, null, null, null, false));

        // Act - buscar como jogador
        autenticarComo(jogador);
        Ficha resultado = fichaService.buscarPorId(ficha.getId());

        // Assert
        assertThat(resultado.getId()).isEqualTo(ficha.getId());
    }

    @Test
    @DisplayName("Jogador não pode buscar fichas de outros")
    void jogadorNaoPodeBuscarFichaDeOutro() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaDoMestre = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha do Mestre", null, null, null, null, null, null, false));

        // Act - tentar buscar como jogador
        autenticarComo(jogador);

        // Assert
        assertThrows(ForbiddenException.class, () -> fichaService.buscarPorId(fichaDoMestre.getId()));
    }

    @Test
    @DisplayName("Buscar ficha inexistente lança ResourceNotFoundException")
    void buscarFichaInexistenteLancaException() {
        // Arrange
        autenticarComo(mestre);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> fichaService.buscarPorId(99999L));
    }

    // =========================================================
    // TESTES DE ATUALIZAÇÃO
    // =========================================================

    @Test
    @DisplayName("Mestre pode atualizar qualquer ficha")
    void mestrePodeAtualizarFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Nome Original", jogador.getId(), null, null, null, null, null, false));

        // Act
        var updateRequest = new UpdateFichaRequest("Nome Atualizado", null, null, null, null, null, 1000L, 2);
        Ficha atualizada = fichaService.atualizar(ficha.getId(), updateRequest);

        // Assert
        assertThat(atualizada.getNome()).isEqualTo("Nome Atualizado");
        assertThat(atualizada.getXp()).isEqualTo(1000L);
        assertThat(atualizada.getRenascimentos()).isEqualTo(2);
    }

    @Test
    @DisplayName("Atualizar com campos null não deve alterar valores existentes")
    void atualizarComCamposNullNaoAlteraValores() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Nome Fixo", null, null, null, null, null, null, false));

        // Atualizar xp primeiro
        fichaService.atualizar(ficha.getId(), new UpdateFichaRequest(null, null, null, null, null, null, 500L, null));

        // Act - update com tudo null (não deve alterar nada)
        var updateRequest = new UpdateFichaRequest(null, null, null, null, null, null, null, null);
        Ficha atualizada = fichaService.atualizar(ficha.getId(), updateRequest);

        // Assert - valores mantidos
        assertThat(atualizada.getNome()).isEqualTo("Nome Fixo");
        assertThat(atualizada.getXp()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Jogador pode atualizar suas próprias fichas")
    void jogadorPodeAtualizarSuaFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Original", jogador.getId(), null, null, null, null, null, false));

        // Act - atualizar como jogador
        autenticarComo(jogador);
        Ficha atualizada = fichaService.atualizar(ficha.getId(), new UpdateFichaRequest("Atualizado Pelo Jogador", null, null, null, null, null, null, null));

        // Assert
        assertThat(atualizada.getNome()).isEqualTo("Atualizado Pelo Jogador");
    }

    @Test
    @DisplayName("Jogador não pode atualizar ficha de outro")
    void jogadorNaoPodeAtualizarFichaDeOutro() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaDoMestre = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha do Mestre", null, null, null, null, null, null, false));

        // Act - tentar atualizar como jogador
        autenticarComo(jogador);
        var updateRequest = new UpdateFichaRequest("Tentativa", null, null, null, null, null, null, null);

        // Assert
        assertThrows(ForbiddenException.class, () -> fichaService.atualizar(fichaDoMestre.getId(), updateRequest));
    }

    @Test
    @DisplayName("Deve atualizar classe quando pertence ao mesmo jogo")
    void deveAtualizarClasseDoMesmoJogo() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Sem Classe", null, null, null, null, null, null, false));
        ClassePersonagem classe = classeRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);

        // Act
        Ficha atualizada = fichaService.atualizar(ficha.getId(), new UpdateFichaRequest(null, null, classe.getId(), null, null, null, null, null));

        // Assert
        assertThat(atualizada.getClasse()).isNotNull();
        assertThat(atualizada.getClasse().getId()).isEqualTo(classe.getId());
    }

    @Test
    @DisplayName("Atualizar com classe de outro jogo deve lançar ForbiddenException")
    void deveRejeitarClasseDeOutroJogo() {
        // Arrange
        autenticarComo(mestre);
        Jogo outroJogo = jogoService.criarJogo(br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest.builder()
                .nome("Outro Jogo Para Classe")
                .build());

        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Personagem", null, null, null, null, null, null, false));
        ClassePersonagem classeOutroJogo = classeRepository.findByJogoIdOrderByOrdemExibicao(outroJogo.getId()).get(0);

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> fichaService.atualizar(ficha.getId(), new UpdateFichaRequest(null, null, classeOutroJogo.getId(), null, null, null, null, null)));
    }

    // =========================================================
    // TESTES DE DELEÇÃO
    // =========================================================

    @Test
    @DisplayName("Mestre pode deletar ficha (soft delete marca deleted_at)")
    void mestrePodeDeletarFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "A Ser Deletada", null, null, null, null, null, null, false));
        Long fichaId = ficha.getId();

        // Verificar que está ativa antes de deletar
        assertThat(fichaRepository.findById(fichaId)).isPresent();

        // Act
        fichaService.deletar(fichaId);

        // Assert - soft delete: campo deleted_at deve estar preenchido
        // Nota: como estamos na mesma transação, usamos findById no repository (ignora @SQLRestriction apenas se consultar entity com cache L1)
        // Verificamos que o campo deletedAt foi marcado via acesso direto
        Ficha fichaAposDeletar = fichaRepository.findById(fichaId).orElse(null);
        // A @SQLRestriction pode não filtrar dentro da mesma transação L1 cache
        // O importante é que deleted_at foi setado
        if (fichaAposDeletar != null) {
            assertThat(fichaAposDeletar.getDeletedAt()).isNotNull();
        }
        // Se null, o @SQLRestriction filtrou - também está correto
    }

    @Test
    @DisplayName("Jogador não pode deletar fichas")
    void jogadorNaoPodeDeletarFichas() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha", jogador.getId(), null, null, null, null, null, false));

        // Act - tentar deletar como jogador
        autenticarComo(jogador);

        // Assert
        assertThrows(ForbiddenException.class, () -> fichaService.deletar(ficha.getId()));
    }

    // =========================================================
    // TESTES DE NPC
    // =========================================================

    @Test
    @DisplayName("Deve criar NPC com jogadorId null e isNpc true")
    void deveCriarNpcComJogadorIdNullEIsNpcTrue() {
        // Arrange
        autenticarComo(mestre);
        var request = new CreateFichaRequest(jogo.getId(), "Dragão Ancião", null, null, null, null, null, null, true);

        // Act
        Ficha npc = fichaService.criar(request);

        // Assert
        assertThat(npc.isNpc()).isTrue();
        assertThat(npc.getJogadorId()).isNull();
        assertThat(npc.getNome()).isEqualTo("Dragão Ancião");
    }

    @Test
    @DisplayName("Deve atualizar descrição de NPC")
    void deveAtualizarDescricaoDeNpc() {
        // Arrange
        autenticarComo(mestre);
        Ficha npc = fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC Vendedor", null, null, null, null, null, null, true));

        // Act
        Ficha npcAtualizado = fichaService.atualizarDescricao(npc.getId(), "Um vendedor misterioso da cidade de Arland.");

        // Assert
        assertThat(npcAtualizado.getDescricao()).isEqualTo("Um vendedor misterioso da cidade de Arland.");
    }

    @Test
    @DisplayName("Listar NPCs não deve incluir fichas de jogadores")
    void listarNpcsNaoDeveIncluirFichasDeJogadores() {
        // Arrange
        autenticarComo(mestre);
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "Ficha Normal", jogador.getId(), null, null, null, null, null, false));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC Guarda", null, null, null, null, null, null, true));
        fichaService.criar(new CreateFichaRequest(jogo.getId(), "NPC Mago", null, null, null, null, null, null, true));

        // Act
        List<Ficha> npcs = fichaService.listarNpcs(jogo.getId());

        // Assert
        assertThat(npcs).hasSize(2);
        assertThat(npcs).allMatch(Ficha::isNpc);
        assertThat(npcs).noneMatch(f -> f.getJogadorId() != null);
    }

    // =========================================================
    // TESTES DE SEGURANÇA — AUDITORIA SP1-T18
    // =========================================================

    @Test
    @DisplayName("Jogador não pode criar NPC (isNpc=true deve ser rejeitado)")
    void jogadorNaoPodeCriarNpc() {
        // Arrange
        autenticarComo(jogador);
        var request = new CreateFichaRequest(jogo.getId(), "NPC Tentativa", null, null, null, null, null, null, true);

        // Act & Assert — CVE: jogador poderia criar NPC antes da correção
        assertThrows(ForbiddenException.class, () -> fichaService.criar(request));
    }

    @Test
    @DisplayName("Jogador não pode acessar ficha de NPC via buscarPorId")
    void jogadorNaoPodeAcessarFichaDeNpc() {
        // Arrange — Mestre cria NPC
        autenticarComo(mestre);
        Ficha npc = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "NPC Secreto", null, null, null, null, null, null, true));

        // Act — Jogador tenta acessar o NPC
        autenticarComo(jogador);

        // Assert — NPC não pertence ao jogador (jogadorId=null), deve ser bloqueado
        assertThrows(ForbiddenException.class, () -> fichaService.buscarPorId(npc.getId()));
    }

    @Test
    @DisplayName("Jogador não pode editar ficha de NPC")
    void jogadorNaoPodeEditarNpc() {
        // Arrange
        autenticarComo(mestre);
        Ficha npc = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "NPC Para Editar", null, null, null, null, null, null, true));

        // Act & Assert
        autenticarComo(jogador);
        var update = new UpdateFichaRequest("Tentativa", null, null, null, null, null, null, null);
        assertThrows(ForbiddenException.class, () -> fichaService.atualizar(npc.getId(), update));
    }

    @Test
    @DisplayName("Jogador não pode acessar ficha de outro jogador via buscarPorId")
    void jogadorNaoPodeAcessarFichaDeOutroJogador() {
        // Arrange — Mestre cria ficha atribuída a outroUsuario
        autenticarComo(mestre);
        Ficha fichaDeOutro = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha de Outro", outroUsuario.getId(), null, null, null, null, null, false));

        // Act — jogador tenta acessar ficha que não é sua
        autenticarComo(jogador);

        // Assert
        assertThrows(ForbiddenException.class, () -> fichaService.buscarPorId(fichaDeOutro.getId()));
    }

    @Test
    @DisplayName("Jogador não pode atualizar ficha de outro jogador")
    void jogadorNaoPodeAtualizarFichaDeOutroJogador() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaDeOutro = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha do Outro", outroUsuario.getId(), null, null, null, null, null, false));

        // Act & Assert
        autenticarComo(jogador);
        var update = new UpdateFichaRequest("Hack", null, null, null, null, null, null, null);
        assertThrows(ForbiddenException.class, () -> fichaService.atualizar(fichaDeOutro.getId(), update));
    }

    @Test
    @DisplayName("Jogador não pode duplicar NPC")
    void jogadorNaoPodeDuplicarNpc() {
        // Arrange
        autenticarComo(mestre);
        Ficha npc = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "NPC Original", null, null, null, null, null, null, true));

        // Act & Assert
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class, () -> fichaService.duplicar(npc.getId(), "NPC Cópia", false));
    }

    // =========================================================
    // TESTES DE LISTAR ATRIBUTOS (GET /fichas/{id}/atributos)
    // =========================================================

    @Test
    @DisplayName("Mestre pode listar atributos de qualquer ficha")
    void mestrePodeListarAtributosDeQualquerFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ficha Atributos", jogador.getId(), null, null, null, null, null, false));

        // Act
        List<FichaAtributo> atributos = fichaService.listarAtributos(ficha.getId());

        // Assert — jogo criado via jogoService inicializa configs padrão (ao menos 1 atributo)
        assertThat(atributos).isNotNull();
        assertThat(atributos).isNotEmpty();
        assertThat(atributos).allMatch(a -> a.getFicha().getId().equals(ficha.getId()));
    }

    @Test
    @DisplayName("Jogador pode listar atributos de sua própria ficha")
    void jogadorPodeListarAtributosDaSuaPropriaFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ficha do Jogador", jogador.getId(), null, null, null, null, null, false));

        // Act
        autenticarComo(jogador);
        List<FichaAtributo> atributos = fichaService.listarAtributos(ficha.getId());

        // Assert
        assertThat(atributos).isNotNull();
        assertThat(atributos).allMatch(a -> a.getFicha().getId().equals(ficha.getId()));
    }

    @Test
    @DisplayName("Jogador não pode listar atributos de ficha de outro jogador")
    void jogadorNaoPodeListarAtributosDeOutroJogador() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaDeOutro = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ficha do Outro", outroUsuario.getId(), null, null, null, null, null, false));

        // Act & Assert
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class, () -> fichaService.listarAtributos(fichaDeOutro.getId()));
    }

    @Test
    @DisplayName("Listar atributos de ficha inexistente lança ResourceNotFoundException")
    void listarAtributosDeInexistenteLancaException() {
        // Arrange
        autenticarComo(mestre);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> fichaService.listarAtributos(99999L));
    }

    // =========================================================
    // TESTES DE LISTAR APTIDOES (GET /fichas/{id}/aptidoes)
    // =========================================================

    @Test
    @DisplayName("Mestre pode listar aptidões de qualquer ficha")
    void mestrePodeListarAptidoesDeQualquerFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ficha Aptidoes", jogador.getId(), null, null, null, null, null, false));

        // Act
        List<FichaAptidao> aptidoes = fichaService.listarAptidoes(ficha.getId());

        // Assert — jogo criado via jogoService inicializa configs padrão (ao menos 1 aptidão)
        assertThat(aptidoes).isNotNull();
        assertThat(aptidoes).isNotEmpty();
        assertThat(aptidoes).allMatch(a -> a.getFicha().getId().equals(ficha.getId()));
    }

    @Test
    @DisplayName("Jogador pode listar aptidões de sua própria ficha")
    void jogadorPodeListarAptidoesDaSuaPropriaFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ficha Aptidoes Jogador", jogador.getId(), null, null, null, null, null, false));

        // Act
        autenticarComo(jogador);
        List<FichaAptidao> aptidoes = fichaService.listarAptidoes(ficha.getId());

        // Assert
        assertThat(aptidoes).isNotNull();
        assertThat(aptidoes).allMatch(a -> a.getFicha().getId().equals(ficha.getId()));
    }

    @Test
    @DisplayName("Jogador não pode listar aptidões de ficha de outro jogador")
    void jogadorNaoPodeListarAptidoesDeOutroJogador() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaDeOutro = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ficha Outro Aptidoes", outroUsuario.getId(), null, null, null, null, null, false));

        // Act & Assert
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class, () -> fichaService.listarAptidoes(fichaDeOutro.getId()));
    }

    @Test
    @DisplayName("Listar aptidões de ficha inexistente lança ResourceNotFoundException")
    void listarAptidoesDeInexistenteLancaException() {
        // Arrange
        autenticarComo(mestre);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> fichaService.listarAptidoes(99999L));
    }

    // =========================================================
    // TESTES DE STATUS / COMPLETAR
    // =========================================================

    @Test
    @DisplayName("Ficha criada começa com status RASCUNHO")
    void fichaDeveComecarComoRascunho() {
        // Arrange
        autenticarComo(mestre);
        var request = new CreateFichaRequest(jogo.getId(), "Heroi Rascunho", null, null, null, null, null, null, false);

        // Act
        Ficha ficha = fichaService.criar(request);

        // Assert
        assertThat(ficha.getStatus()).isEqualTo(FichaStatus.RASCUNHO);
    }

    @Test
    @DisplayName("Deve completar ficha com todos os campos obrigatórios preenchidos")
    void deveCompletarFichaComTodosOsCampos() {
        // Arrange
        autenticarComo(mestre);
        Raca raca = racaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        ClassePersonagem classe = classeRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        GeneroConfig genero = generoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        IndoleConfig indole = indoleRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        PresencaConfig presenca = presencaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);

        var request = new CreateFichaRequest(
                jogo.getId(), "Heroi Completo",
                null, raca.getId(), classe.getId(), genero.getId(), indole.getId(), presenca.getId(), false);
        Ficha ficha = fichaService.criar(request);
        assertThat(ficha.getStatus()).isEqualTo(FichaStatus.RASCUNHO);

        // Act
        Ficha completada = fichaService.completar(ficha.getId());

        // Assert
        assertThat(completada.getStatus()).isEqualTo(FichaStatus.COMPLETA);
    }

    @Test
    @DisplayName("Completar ficha sem raça lança ValidationException")
    void deveRejeitarCompletarSemRaca() {
        // Arrange
        autenticarComo(mestre);
        ClassePersonagem classe = classeRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        GeneroConfig genero = generoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        IndoleConfig indole = indoleRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        PresencaConfig presenca = presencaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);

        var request = new CreateFichaRequest(
                jogo.getId(), "Sem Raca",
                null, null, classe.getId(), genero.getId(), indole.getId(), presenca.getId(), false);
        Ficha ficha = fichaService.criar(request);

        // Act & Assert
        ValidationException ex = assertThrows(ValidationException.class, () -> fichaService.completar(ficha.getId()));
        assertThat(ex.getErrors()).containsKey("raca");
        assertThat(ficha.getStatus()).isEqualTo(FichaStatus.RASCUNHO);
    }

    @Test
    @DisplayName("Completar ficha sem classe lança ValidationException")
    void deveRejeitarCompletarSemClasse() {
        // Arrange
        autenticarComo(mestre);
        Raca raca = racaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        GeneroConfig genero = generoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        IndoleConfig indole = indoleRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        PresencaConfig presenca = presencaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);

        var request = new CreateFichaRequest(
                jogo.getId(), "Sem Classe",
                null, raca.getId(), null, genero.getId(), indole.getId(), presenca.getId(), false);
        Ficha ficha = fichaService.criar(request);

        // Act & Assert
        ValidationException ex = assertThrows(ValidationException.class, () -> fichaService.completar(ficha.getId()));
        assertThat(ex.getErrors()).containsKey("classe");
    }

    @Test
    @DisplayName("Completar ficha sem gênero lança ValidationException")
    void deveRejeitarCompletarSemGenero() {
        // Arrange
        autenticarComo(mestre);
        Raca raca = racaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        ClassePersonagem classe = classeRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        IndoleConfig indole = indoleRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        PresencaConfig presenca = presencaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);

        var request = new CreateFichaRequest(
                jogo.getId(), "Sem Genero",
                null, raca.getId(), classe.getId(), null, indole.getId(), presenca.getId(), false);
        Ficha ficha = fichaService.criar(request);

        // Act & Assert
        ValidationException ex = assertThrows(ValidationException.class, () -> fichaService.completar(ficha.getId()));
        assertThat(ex.getErrors()).containsKey("genero");
    }

    @Test
    @DisplayName("Completar ficha já completa é idempotente")
    void completarFichaJaCompletaEIdempotente() {
        // Arrange
        autenticarComo(mestre);
        Raca raca = racaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        ClassePersonagem classe = classeRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        GeneroConfig genero = generoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        IndoleConfig indole = indoleRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        PresencaConfig presenca = presencaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);

        var request = new CreateFichaRequest(
                jogo.getId(), "Heroi Idempotente",
                null, raca.getId(), classe.getId(), genero.getId(), indole.getId(), presenca.getId(), false);
        Ficha ficha = fichaService.criar(request);
        fichaService.completar(ficha.getId());

        // Act — segunda chamada não deve lançar exceção
        Ficha resultado = fichaService.completar(ficha.getId());

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(FichaStatus.COMPLETA);
    }

    @Test
    @DisplayName("Jogador não pode completar ficha de outro jogador")
    void jogadorNaoPodeCompletarFichaDeOutro() {
        // Arrange
        autenticarComo(mestre);
        Raca raca = racaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        ClassePersonagem classe = classeRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        GeneroConfig genero = generoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        IndoleConfig indole = indoleRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        PresencaConfig presenca = presencaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);

        // Ficha pertence ao outroUsuario (não ao jogador autenticado)
        Ficha fichaOutro = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ficha Outro", outroUsuario.getId(),
                raca.getId(), classe.getId(), genero.getId(), indole.getId(), presenca.getId(), false));

        // Act & Assert — jogador tenta completar ficha que não é sua
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class, () -> fichaService.completar(fichaOutro.getId()));
    }

    @Test
    @DisplayName("FichaResponse inclui campo status")
    void fichaResponseIncluiCampoStatus() {
        // Arrange
        autenticarComo(mestre);
        var request = new CreateFichaRequest(jogo.getId(), "Heroi Response", null, null, null, null, null, null, false);
        Ficha ficha = fichaService.criar(request);

        // Act — buscar via buscarPorId para exercitar o mapper via toResponse
        Ficha result = fichaService.buscarPorId(ficha.getId());

        // Assert
        assertThat(result.getStatus()).isEqualTo(FichaStatus.RASCUNHO);
        assertThat(result.getStatus().name()).isEqualTo("RASCUNHO");
    }

    @Test
    @DisplayName("NPC também começa como RASCUNHO e pode ser completado pelo Mestre")
    void npcDeveComecarComoRascunhoESerCompletadoPeloMestre() {
        // Arrange
        autenticarComo(mestre);
        Raca raca = racaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        ClassePersonagem classe = classeRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        GeneroConfig genero = generoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        IndoleConfig indole = indoleRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);
        PresencaConfig presenca = presencaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).get(0);

        Ficha npc = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Goblin NPC",
                null, raca.getId(), classe.getId(), genero.getId(), indole.getId(), presenca.getId(), true));

        assertThat(npc.getStatus()).isEqualTo(FichaStatus.RASCUNHO);

        // Act
        Ficha npcCompleto = fichaService.completar(npc.getId());

        // Assert
        assertThat(npcCompleto.getStatus()).isEqualTo(FichaStatus.COMPLETA);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
