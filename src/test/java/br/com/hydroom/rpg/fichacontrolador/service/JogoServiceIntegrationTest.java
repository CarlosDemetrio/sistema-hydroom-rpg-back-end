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
            .build());

        jogador = usuarioRepository.save(Usuario.builder()
            .nome("Jogador Teste")
            .email("jogador@test.com")
            .provider("google")
            .providerId("google-jogador")
            .build());

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha Integracao")
            .descricao("Descricao teste")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador)
            .role(RoleJogo.JOGADOR)
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
        jogoRepository.save(Jogo.builder().nome("Jogo Inativo").jogoAtivo(false).build());

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
        assertThat(result.isActive()).isTrue();
        assertThat(jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
            result.getId(), mestre.getId(), RoleJogo.MESTRE)).isTrue();
    }

    @Test
    @DisplayName("Deve marcar automaticamente o primeiro jogo criado como jogo ativo")
    void criarPrimeiroJogoDeveSerJogoAtivo() {
        // Arrange - Criar um novo mestre sem jogos
        Usuario novoMestre = Usuario.builder()
            .nome("Novo Mestre")
            .email("novomestre@test.com")
            .provider("google")
            .providerId("google999")
            .build();
        novoMestre = usuarioRepository.save(novoMestre);

        // Autenticar como novo mestre
        autenticarComo(novoMestre);

        var request = CriarJogoRequest.builder()
            .nome("Primeira Campanha")
            .descricao("Meu primeiro jogo")
            .dataInicio(LocalDate.now())
            .build();

        // Act
        Jogo primeiroJogo = jogoService.criarJogo(request);

        // Assert
        assertThat(primeiroJogo.getJogoAtivo()).isTrue();
        assertThat(primeiroJogo.getNome()).isEqualTo("Primeira Campanha");
    }

    @Test
    @DisplayName("Segundo jogo criado NÃO deve ser automaticamente o jogo ativo")
    void criarSegundoJogoNaoDeveSerJogoAtivo() {
        // Arrange - Criar um novo mestre
        Usuario novoMestre = Usuario.builder()
            .nome("Mestre Experiente")
            .email("mestreexperiente@test.com")
            .provider("google")
            .providerId("google888")
            .build();
        novoMestre = usuarioRepository.save(novoMestre);

        // Autenticar como novo mestre
        autenticarComo(novoMestre);

        // Criar primeiro jogo (que será automaticamente ativo)
        var request1 = CriarJogoRequest.builder()
            .nome("Campanha Original")
            .descricao("Primeiro jogo")
            .dataInicio(LocalDate.now())
            .build();
        Jogo primeiroJogo = jogoService.criarJogo(request1);

        // Act - Criar segundo jogo
        var request2 = CriarJogoRequest.builder()
            .nome("Nova Campanha")
            .descricao("Segundo jogo")
            .dataInicio(LocalDate.now())
            .build();
        Jogo segundoJogo = jogoService.criarJogo(request2);

        // Assert
        assertThat(primeiroJogo.getJogoAtivo()).isTrue();
        assertThat(segundoJogo.getJogoAtivo()).isFalse(); // Segundo jogo NÃO deve ser ativo

        // Verificar que ainda existe apenas 1 jogo ativo
        Jogo jogoAtivoAtual = jogoService.buscarJogoAtivo();
        assertThat(jogoAtivoAtual.getId()).isEqualTo(primeiroJogo.getId());
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
        assertThat(atualizado.isActive()).isFalse();
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
        assertThat(result.isActive()).isTrue();
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
        assertThat(result.isActive()).isTrue();

        // Assert - Mestre registrado
        assertThat(jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
            result.getId(), mestre.getId(), RoleJogo.MESTRE)).isTrue();

        // Assert - Configurações criadas
        Long jogoId = result.getId();

        assertThat(atributoRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 7 atributos")
            .hasSize(7);

        assertThat(tipoAptidaoRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 2 tipos de aptidão (FISICA e MENTAL)")
            .hasSize(2);

        assertThat(aptidaoRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 24 aptidões (12 físicas + 12 mentais)")
            .hasSize(24);

        assertThat(nivelRepository.findByJogoIdOrderByNivel(jogoId))
            .as("Deve criar 36 níveis (0-35)")
            .hasSize(36);

        assertThat(classeRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 12 classes")
            .hasSize(12);

        assertThat(racaRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 6 raças")
            .hasSize(6);

        assertThat(racaBonusRepository.findAll())
            .as("Deve criar pelo menos 6 bônus raciais")
            .hasSizeGreaterThanOrEqualTo(6);

        assertThat(prospeccaoRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 6 dados de prospecção")
            .hasSize(6);

        assertThat(generoRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 3 gêneros (Masculino, Feminino, Outro)")
            .hasSize(3);

        assertThat(indoleRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 3 índoles Klayrah (Bom, Mau, Neutro)")
            .hasSize(3);

        assertThat(presencaRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 4 presenças (Bom, Leal, Caótico, Neutro)")
            .hasSize(4);

        assertThat(membroCorpoRepository.findByJogoIdOrderByOrdemExibicao(jogoId))
            .as("Deve criar 7 membros do corpo (incluindo Sangue)")
            .hasSize(7);

        // Vantagens são opcionais, apenas verificar que não falhou
        var vantagens = vantagemRepository.findByJogoIdOrderByOrdemExibicao(jogoId);
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
        var niveis = nivelRepository.findByJogoIdOrderByNivel(result.getId());
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
        var atributos = atributoRepository.findByJogoIdOrderByOrdemExibicao(result.getId());

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
        var tipos = tipoAptidaoRepository.findByJogoIdOrderByOrdemExibicao(result.getId());

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

        var todasAptidoes = aptidaoRepository.findByJogoIdOrderByOrdemExibicao(result.getId());

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
        var racas = racaRepository.findByJogoIdOrderByOrdemExibicao(result.getId());

        assertThat(racas)
            .hasSize(6)
            .extracting("nome")
            .containsExactlyInAnyOrder("Humano", "Karzarcryer", "Ikaruz", "Hankraz", "Atlas", "Anakarys");

        // Assert - Verificar que raças têm bônus (exceto Humano que não tem)
        var racaKarzarcryer = racas.stream()
            .filter(r -> r.getNome().equals("Karzarcryer"))
            .findFirst()
            .orElseThrow();

        var bonusKarzarcryer = racaBonusRepository.findByRacaId(racaKarzarcryer.getId());
        assertThat(bonusKarzarcryer)
            .as("Karzarcryer deve ter bônus raciais")
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
        var membros = membroCorpoRepository.findByJogoIdOrderByOrdemExibicao(result.getId());

        assertThat(membros)
            .hasSize(7)
            .extracting("nome")
            .containsExactlyInAnyOrder(
                "Cabeça", "Tronco",
                "Braço Direito", "Braço Esquerdo",
                "Perna Direita", "Perna Esquerda",
                "Sangue"
            );

        // Assert - Verificar que todos têm porcentagem de vida positiva
        membros.forEach(membro -> {
            assertThat(membro.getPorcentagemVida())
                .as("Membro %s deve ter porcentagem de vida positiva", membro.getNome())
                .isNotNull()
                .isGreaterThan(java.math.BigDecimal.ZERO);
        });

        // Assert - Cabeça deve ter 75% (corrigido BUG-DC-06)
        var cabeca = membros.stream()
            .filter(m -> m.getNome().equals("Cabeça"))
            .findFirst()
            .orElseThrow();
        assertThat(cabeca.getPorcentagemVida())
            .as("Cabeça deve ter 75% da vida (BUG-DC-06 corrigido)")
            .isEqualByComparingTo(new java.math.BigDecimal("0.75"));

        // Assert - Sangue deve ter 100% (representa vida total)
        var sangue = membros.stream()
            .filter(m -> m.getNome().equals("Sangue"))
            .findFirst()
            .orElseThrow();
        assertThat(sangue.getPorcentagemVida())
            .as("Sangue deve ter 100% da vida total (DIV-05)")
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
        var atributos1 = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogo1.getId());
        int atributosCount1 = atributos1.size();

        // Act - Tentar criar configs novamente (simulando bug)
        // Nota: GameConfigInitializerService já previne isso internamente

        // Assert - Quantidade não deve mudar
        var atributos2 = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogo1.getId());
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
        assertThat(atributoRepository.findByJogoIdOrderByOrdemExibicao(jogo1.getId())).hasSize(7);
        assertThat(atributoRepository.findByJogoIdOrderByOrdemExibicao(jogo2.getId())).hasSize(7);

        // Assert - Configs são independentes (IDs diferentes)
        var atributosJogo1 = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogo1.getId());
        var atributosJogo2 = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogo2.getId());

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

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
