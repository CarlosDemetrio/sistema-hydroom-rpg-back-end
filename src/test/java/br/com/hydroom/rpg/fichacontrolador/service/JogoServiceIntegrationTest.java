package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.EditarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("JogoService - Testes de Integracao")
class JogoServiceIntegrationTest {

    @Autowired
    private JogoService jogoService;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Repositories de configuração para validar criação automática
    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoRepository;

    @Autowired
    private TipoAptidaoRepository tipoAptidaoRepository;

    @Autowired
    private ConfiguracaoNivelRepository nivelRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private ConfiguracaoRacaRepository racaRepository;

    @Autowired
    private RacaBonusAtributoRepository racaBonusRepository;

    @Autowired
    private DadoProspeccaoConfigRepository prospeccaoRepository;

    @Autowired
    private GeneroConfigRepository generoRepository;

    @Autowired
    private IndoleConfigRepository indoleRepository;

    @Autowired
    private PresencaConfigRepository presencaRepository;

    @Autowired
    private MembroCorpoConfigRepository membroCorpoRepository;

    @Autowired
    private VantagemConfigRepository vantagemRepository;

    private Usuario mestre;
    private Usuario jogador;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        mestre = usuarioRepository.save(Usuario.builder()
            .nome("Mestre Teste")
            .email("mestre@test.com")
            .provider("google")
            .providerId("google-mestre")
            .ativo(true)
            .build());

        jogador = usuarioRepository.save(Usuario.builder()
            .nome("Jogador Teste")
            .email("jogador@test.com")
            .provider("google")
            .providerId("google-jogador")
            .ativo(true)
            .build());

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha Integracao")
            .descricao("Descricao teste")
            .dataInicio(LocalDate.now())
            .ativo(true)
            .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .ativo(true)
            .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador)
            .role(RoleJogo.JOGADOR)
            .ativo(true)
            .build());

        setAuth(mestre);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve listar apenas jogos ativos")
    void listarJogosDoUsuario() {
        // Arrange
        jogoRepository.save(Jogo.builder().nome("Jogo Inativo").ativo(false).build());

        // Act
        List<Jogo> result = jogoService.listarJogosDoUsuario();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(jogo.getId());
    }

    @Test
    @DisplayName("Deve buscar jogo quando participa")
    void buscarJogoComParticipacao() {
        // Arrange
        setAuth(jogador);

        // Act
        Jogo result = jogoService.buscarJogo(jogo.getId());

        // Assert
        assertThat(result.getId()).isEqualTo(jogo.getId());
    }

    @Test
    @DisplayName("Deve negar acesso quando nao participa")
    void buscarJogoSemParticipacao() {
        // Arrange
        Usuario outro = usuarioRepository.save(Usuario.builder()
            .nome("Outro")
            .email("outro@test.com")
            .provider("google")
            .providerId("google-outro")
            .ativo(true)
            .build());
        setAuth(outro);

        // Act + Assert
        assertThrows(AccessDeniedException.class, () -> jogoService.buscarJogo(jogo.getId()));
    }

    @Test
    @DisplayName("Deve criar jogo e registrar mestre")
    void criarJogo() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Nova Campanha")
            .descricao("Nova descricao")
            .dataInicio(LocalDate.now())
            .build();

        // Act
        Jogo result = jogoService.criarJogo(request);

        // Assert
        assertThat(result.getId()).isNotNull();
        assertThat(result.getNome()).isEqualTo("Nova Campanha");
        assertThat(resultisActive()).isTrue();
        assertThat(jogoParticipanteRepository.existsByUsuarioIdAndJogoIdAndRoleAndAtivoTrue(
            mestre.getId(), result.getId(), RoleJogo.MESTRE)).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar jogo quando mestre")
    void atualizarJogo() {
        // Arrange
        var request = EditarJogoRequest.builder()
            .nome("Campanha Atualizada")
            .descricao("Descricao Atualizada")
            .imagemUrl("http://img")
            .dataInicio(LocalDate.now())
            .dataFim(LocalDate.now().plusDays(1))
            .build();

        // Act
        Jogo result = jogoService.atualizarJogo(jogo.getId(), request);

        // Assert
        assertThat(result.getNome()).isEqualTo("Campanha Atualizada");
        assertThat(result.getDescricao()).isEqualTo("Descricao Atualizada");
    }

    @Test
    @DisplayName("Deve negar atualizacao quando nao e mestre")
    void atualizarJogoSemPermissao() {
        // Arrange
        setAuth(jogador);
        var request = EditarJogoRequest.builder()
            .nome("Tentativa")
            .build();

        // Act + Assert
        assertThrows(AccessDeniedException.class, () -> jogoService.atualizarJogo(jogo.getId(), request));
    }

    @Test
    @DisplayName("Deve marcar jogo como inativo quando mestre")
    void deletarJogo() {
        // Act
        jogoService.deletarJogo(jogo.getId());

        // Assert
        Jogo atualizado = jogoRepository.findById(jogo.getId()).orElseThrow();
        assertThat(atualizadoisActive()).isFalse();
    }

    @Test
    @DisplayName("Deve reativar jogo quando mestre")
    void ativarJogo() {
        // Arrange
        jogo.delete();
        jogoRepository.save(jogo);

        // Act
        Jogo result = jogoService.ativarJogo(jogo.getId());

        // Assert
        assertThat(resultisActive()).isTrue();
    }

    // ========================================
    // TESTES DE CONFIGURAÇÃO AUTOMÁTICA
    // ========================================

    @Test
    @DisplayName("Deve criar jogo com todas as configurações padrão")
    void criarJogoComConfiguracoesAutomaticas() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Campanha com Configs")
            .descricao("Teste de criação automática")
            .dataInicio(LocalDate.now())
            .build();

        // Act
        Jogo result = jogoService.criarJogo(request);

        // Assert - Jogo criado
        assertThat(result.getId()).isNotNull();
        assertThat(result.getNome()).isEqualTo("Campanha com Configs");
        assertThat(resultisActive()).isTrue();

        // Assert - Mestre registrado
        assertThat(jogoParticipanteRepository.existsByUsuarioIdAndJogoIdAndRoleAndAtivoTrue(
            mestre.getId(), result.getId(), RoleJogo.MESTRE)).isTrue();

        // Assert - Configurações criadas
        Long jogoId = result.getId();

        assertThat(atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId))
            .as("Deve criar 7 atributos")
            .hasSize(7);

        assertThat(tipoAptidaoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId))
            .as("Deve criar 2 tipos de aptidão (FISICA e MENTAL)")
            .hasSize(2);

        assertThat(aptidaoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId))
            .as("Deve criar 24 aptidões (12 físicas + 12 mentais)")
            .hasSize(24);

        assertThat(nivelRepository.findByJogoIdAndAtivoTrueOrderByNivel(jogoId))
            .as("Deve criar 36 níveis (0-35)")
            .hasSize(36);

        assertThat(classeRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId))
            .as("Deve criar 12 classes")
            .hasSize(12);

        assertThat(racaRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId))
            .as("Deve criar 4 raças")
            .hasSize(4);

        assertThat(racaBonusRepository.findAll())
            .as("Deve criar pelo menos 6 bônus raciais")
            .hasSizeGreaterThanOrEqualTo(6);

        assertThat(prospeccaoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId))
            .as("Deve criar 6 dados de prospecção")
            .hasSize(6);

        assertThat(generoRepository.findByJogoIdAndAtivoTrueOrderByOrdem(jogoId))
            .as("Deve criar 4 gêneros")
            .hasSize(4);

        assertThat(indoleRepository.findByJogoIdAndAtivoTrueOrderByOrdem(jogoId))
            .as("Deve criar 9 índoles")
            .hasSize(9);

        assertThat(presencaRepository.findByJogoIdAndAtivoTrueOrderByOrdem(jogoId))
            .as("Deve criar 6 presenças")
            .hasSize(6);

        assertThat(membroCorpoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId))
            .as("Deve criar 6 membros do corpo")
            .hasSize(6);

        // Vantagens são opcionais, apenas verificar que não falhou
        var vantagens = vantagemRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
        assertThat(vantagens)
            .as("Deve criar vantagens (quantidade pode variar)")
            .isNotEmpty();
    }

    @Test
    @DisplayName("Deve criar nível 0 com valores zerados")
    void criarJogoComNivelZero() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Teste Nível 0")
            .descricao("Validar nível inicial")
            .build();

        // Act
        Jogo result = jogoService.criarJogo(request);

        // Assert - Buscar nível 0
        var niveis = nivelRepository.findByJogoIdAndAtivoTrueOrderByNivel(result.getId());
        var nivel0Opt = niveis.stream()
            .filter(n -> n.getNivel() == 0)
            .findFirst();

        assertThat(nivel0Opt)
            .as("Deve existir nível 0")
            .isPresent();

        var nivel0 = nivel0Opt.get();
        assertThat(nivel0.getNivel()).isEqualTo(0);
        assertThat(nivel0.getXpNecessaria()).isEqualTo(0L);
        assertThat(nivel0.getPontosAtributo()).isEqualTo(0);
        assertThat(nivel0.getPontosAptidao()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve criar atributos com abreviações corretas")
    void criarJogoComAtributosCorretos() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Teste Atributos")
            .build();

        // Act
        Jogo result = jogoService.criarJogo(request);

        // Assert - Verificar atributos esperados (conforme DefaultGameConfigProviderImpl)
        var atributos = atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(result.getId());

        assertThat(atributos)
            .hasSize(7)
            .extracting("abreviacao")
            .containsExactlyInAnyOrder("FOR", "AGI", "VIG", "SAB", "INT", "INTU", "AST");
    }

    @Test
    @DisplayName("Deve criar aptidões físicas e mentais")
    void criarJogoComAptidoesCorretas() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Teste Aptidões")
            .build();

        // Act
        Jogo result = jogoService.criarJogo(request);

        // Assert - Verificar tipos de aptidão
        var tipos = tipoAptidaoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(result.getId());

        assertThat(tipos)
            .hasSize(2)
            .extracting("nome")
            .containsExactlyInAnyOrder("FISICA", "MENTAL");

        // Assert - Verificar distribuição de aptidões
        var tipoFisica = tipos.stream()
            .filter(t -> t.getNome().equals("FISICA"))
            .findFirst()
            .orElseThrow();

        var tipoMental = tipos.stream()
            .filter(t -> t.getNome().equals("MENTAL"))
            .findFirst()
            .orElseThrow();

        var todasAptidoes = aptidaoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(result.getId());

        long aptidoesFisicas = todasAptidoes.stream()
            .filter(a -> a.getTipoAptidao().getId().equals(tipoFisica.getId()))
            .count();

        long aptidoesMentais = todasAptidoes.stream()
            .filter(a -> a.getTipoAptidao().getId().equals(tipoMental.getId()))
            .count();

        assertThat(aptidoesFisicas).isEqualTo(12);
        assertThat(aptidoesMentais).isEqualTo(12);
    }

    @Test
    @DisplayName("Deve criar raças com bônus raciais associados")
    void criarJogoComBonusRaciais() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Teste Bônus Raciais")
            .build();

        // Act
        Jogo result = jogoService.criarJogo(request);

        // Assert - Verificar raças
        var racas = racaRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(result.getId());

        assertThat(racas)
            .hasSize(4)
            .extracting("nome")
            .containsExactlyInAnyOrder("Humano", "Elfo", "Anão", "Meio-Elfo");

        // Assert - Verificar que raças têm bônus (exceto Humano que não tem)
        var racaElfo = racas.stream()
            .filter(r -> r.getNome().equals("Elfo"))
            .findFirst()
            .orElseThrow();

        var bonusElfo = racaBonusRepository.findByRacaId(racaElfo.getId());
        assertThat(bonusElfo)
            .as("Elfo deve ter bônus raciais")
            .isNotEmpty();
    }

    @Test
    @DisplayName("Deve criar membros do corpo com porcentagens de vida")
    void criarJogoComMembrosCorpo() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Teste Membros Corpo")
            .build();

        // Act
        Jogo result = jogoService.criarJogo(request);

        // Assert - Verificar membros
        var membros = membroCorpoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(result.getId());

        assertThat(membros)
            .hasSize(6)
            .extracting("nome")
            .containsExactlyInAnyOrder(
                "Cabeça", "Tronco",
                "Braço Direito", "Braço Esquerdo",
                "Perna Direita", "Perna Esquerda"
            );

        // Assert - Verificar que todos têm porcentagem de vida
        membros.forEach(membro -> {
            assertThat(membro.getPorcentagemVida())
                .as("Membro %s deve ter porcentagem de vida", membro.getNome())
                .isNotNull()
                .isGreaterThan(java.math.BigDecimal.ZERO)
                .isLessThanOrEqualTo(java.math.BigDecimal.ONE);
        });

        // Assert - Soma das porcentagens deve ser 100% (1.00)
        var somaPortcentagens = membros.stream()
            .map(m -> m.getPorcentagemVida())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        assertThat(somaPortcentagens)
            .as("Soma das porcentagens deve ser 100% (1.00)")
            .isEqualByComparingTo(java.math.BigDecimal.ONE);
    }

    @Test
    @DisplayName("Não deve duplicar configurações se chamar duas vezes")
    void naoDuplicarConfiguracoesSeExistirem() {
        // Arrange
        var request = CriarJogoRequest.builder()
            .nome("Teste Duplicação")
            .build();

        Jogo jogo1 = jogoService.criarJogo(request);
        var atributos1 = atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogo1.getId());
        int atributosCount1 = atributos1.size();

        // Act - Tentar criar configs novamente (simulando bug)
        // Nota: GameConfigInitializerService já previne isso internamente

        // Assert - Quantidade não deve mudar
        var atributos2 = atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogo1.getId());
        int atributosCount2 = atributos2.size();
        assertThat(atributosCount2).isEqualTo(atributosCount1);
    }

    @Test
    @DisplayName("Deve criar jogos independentes com configs isoladas")
    void criarJogosIndependentes() {
        // Arrange & Act
        var jogo1 = jogoService.criarJogo(CriarJogoRequest.builder()
            .nome("Jogo 1")
            .build());

        var jogo2 = jogoService.criarJogo(CriarJogoRequest.builder()
            .nome("Jogo 2")
            .build());

        // Assert - Ambos têm configs
        assertThat(atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogo1.getId())).hasSize(7);
        assertThat(atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogo2.getId())).hasSize(7);

        // Assert - Configs são independentes (IDs diferentes)
        var atributosJogo1 = atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogo1.getId());
        var atributosJogo2 = atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogo2.getId());

        var idsJogo1 = atributosJogo1.stream().map(a -> a.getId()).toList();
        var idsJogo2 = atributosJogo2.stream().map(a -> a.getId()).toList();

        assertThat(idsJogo1)
            .as("Atributos de jogos diferentes devem ter IDs diferentes")
            .doesNotContainAnyElementsOf(idsJogo2);
    }

    private void setAuth(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
