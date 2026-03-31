package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
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
 * Testes de integração para o método FichaService.duplicar().
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Cópia de todos os sub-registros (FichaAtributo, FichaAptidao, FichaBonus,
 *       FichaVida, FichaEssencia, FichaAmeaca, FichaProspeccao)</li>
 *   <li>Nome distinto e mesmo jogoId na cópia</li>
 *   <li>danoRecebido = 0 em FichaVidaMembro na cópia</li>
 *   <li>Restrição de acesso: Jogador não pode duplicar ficha de outro</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaService.duplicar() - Testes de Integração")
class FichaDuplicacaoIntegrationTest {

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
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroJogador;
    private Jogo jogo;
    private Ficha fichaOriginal;

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
                .nome("Mestre Duplicacao")
                .email("mestre.dup" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-dup-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Duplicacao")
                .email("jogador.dup" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-dup-" + n)
                .role("JOGADOR")
                .build());

        outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Jogador Duplicacao")
                .email("outro.dup" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-dup-" + n)
                .role("JOGADOR")
                .build());

        // Criar jogo como Mestre (inicializa configs automaticamente)
        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Campanha Duplicacao " + n)
                .build());

        // Adicionar jogador como participante aprovado
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        // Criar ficha original como Mestre (com jogadorId do jogador)
        fichaOriginal = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem Original",
                        jogador.getId(), null, null, null, null, null, false));
    }

    // =========================================================
    // TESTES DE DUPLICAÇÃO
    // =========================================================

    @Test
    @DisplayName("Deve duplicar ficha com todos os sub-registros copiados")
    void deveDuplicarFichaComTodosSubRegistros() {
        // Arrange
        Long fichaId = fichaOriginal.getId();

        int qtdAtributos = fichaAtributoRepository.findByFichaId(fichaId).size();
        int qtdAptidoes = fichaAptidaoRepository.findByFichaId(fichaId).size();
        int qtdBonus = fichaBonusRepository.findByFichaId(fichaId).size();
        int qtdMembros = fichaVidaMembroRepository.findByFichaId(fichaId).size();
        int qtdProspeccoes = fichaProspeccaoRepository.findByFichaId(fichaId).size();

        // Act - Mestre duplica a ficha
        autenticarComo(mestre);
        Ficha copia = fichaService.duplicar(fichaId, "Cópia do Personagem", true);

        // Assert - cópia criada
        assertThat(copia.getId()).isNotNull();
        assertThat(copia.getId()).isNotEqualTo(fichaId);

        Long copiaId = copia.getId();

        // Assert - FichaAtributo copiado
        assertThat(fichaAtributoRepository.findByFichaId(copiaId)).hasSize(qtdAtributos);

        // Assert - FichaAptidao copiado
        assertThat(fichaAptidaoRepository.findByFichaId(copiaId)).hasSize(qtdAptidoes);

        // Assert - FichaBonus copiado
        assertThat(fichaBonusRepository.findByFichaId(copiaId)).hasSize(qtdBonus);

        // Assert - FichaVida copiada (1 por ficha)
        assertThat(fichaVidaRepository.findByFichaId(copiaId)).isPresent();

        // Assert - FichaVidaMembro copiado
        assertThat(fichaVidaMembroRepository.findByFichaId(copiaId)).hasSize(qtdMembros);

        // Assert - FichaEssencia copiada
        assertThat(fichaEssenciaRepository.findByFichaId(copiaId)).isPresent();

        // Assert - FichaAmeaca copiada
        assertThat(fichaAmeacaRepository.findByFichaId(copiaId)).isPresent();

        // Assert - FichaProspeccao copiada
        assertThat(fichaProspeccaoRepository.findByFichaId(copiaId)).hasSize(qtdProspeccoes);
    }

    @Test
    @DisplayName("Cópia deve ter nome distinto e mesmo jogoId da original")
    void deveDuplicarFichaComNomeDistinto() {
        // Arrange
        String novoNome = "Nova Versão do Personagem";

        // Act
        autenticarComo(mestre);
        Ficha copia = fichaService.duplicar(fichaOriginal.getId(), novoNome, false);

        // Assert
        assertThat(copia.getNome()).isEqualTo(novoNome);
        assertThat(copia.getNome()).isNotEqualTo(fichaOriginal.getNome());
        assertThat(copia.getJogo().getId()).isEqualTo(fichaOriginal.getJogo().getId());
    }

    @Test
    @DisplayName("FichaVidaMembro na cópia deve ter danoRecebido = 0")
    void deveDuplicarFichaComDanoZerado() {
        // Arrange - simular que a ficha original tem dano nos membros
        Long fichaId = fichaOriginal.getId();
        List<FichaVidaMembro> membrosOriginais = fichaVidaMembroRepository.findByFichaId(fichaId);

        // Se existem membros, simular dano
        if (!membrosOriginais.isEmpty()) {
            FichaVidaMembro primeiro = membrosOriginais.get(0);
            primeiro.setDanoRecebido(10);
            fichaVidaMembroRepository.save(primeiro);
        }

        // Act - duplicar ficha
        autenticarComo(mestre);
        Ficha copia = fichaService.duplicar(fichaId, "Cópia Sem Dano", true);

        // Assert - todos os membros da cópia têm danoRecebido = 0
        List<FichaVidaMembro> membrosCopia = fichaVidaMembroRepository.findByFichaId(copia.getId());
        assertThat(membrosCopia).allMatch(m -> m.getDanoRecebido() == 0);
    }

    @Test
    @DisplayName("Jogador não pode duplicar ficha de outro")
    void deveImpedirJogadorDeDuplicarFichaDeOutro() {
        // Arrange - criar ficha pertencente ao Mestre (sem jogadorId)
        autenticarComo(mestre);
        Ficha fichaDeMestre = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha do Mestre",
                        null, null, null, null, null, null, false));

        // Act - outroJogador (não é dono) tenta duplicar a ficha do Mestre
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(outroJogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        autenticarComo(outroJogador);

        // Assert
        assertThrows(ForbiddenException.class,
                () -> fichaService.duplicar(fichaDeMestre.getId(), "Cópia Indevida", false));
    }

    @Test
    @DisplayName("Mestre pode duplicar qualquer ficha")
    void deveMestrePoderDuplicarQualquerFicha() {
        // Arrange - fichaOriginal pertence ao jogador
        autenticarComo(mestre);

        // Act
        Ficha copia = fichaService.duplicar(fichaOriginal.getId(), "Cópia pelo Mestre", false);

        // Assert
        assertThat(copia.getId()).isNotNull();
        assertThat(copia.getNome()).isEqualTo("Cópia pelo Mestre");
    }

    @Test
    @DisplayName("Jogador pode duplicar sua própria ficha")
    void deveJogadorPoderDuplicarSuaFicha() {
        // Arrange - fichaOriginal pertence ao jogador
        autenticarComo(jogador);

        // Act
        Ficha copia = fichaService.duplicar(fichaOriginal.getId(), "Minha Segunda Ficha", true);

        // Assert
        assertThat(copia.getId()).isNotNull();
        assertThat(copia.getNome()).isEqualTo("Minha Segunda Ficha");
        // Jogador duplica para si mesmo
        assertThat(copia.getJogadorId()).isEqualTo(jogador.getId());
    }

    @Test
    @DisplayName("Cópia de NPC deve manter isNpc=true e jogadorId=null")
    void deveDuplicarNpcComIsNpcTrueEJogadorIdNull() {
        // Arrange - criar NPC
        autenticarComo(mestre);
        Ficha npcOriginal = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Goblin Original",
                        null, null, null, null, null, null, true));

        // Act
        Ficha copiaNpc = fichaService.duplicar(npcOriginal.getId(), "Goblin Cópia", false);

        // Assert
        assertThat(copiaNpc.isNpc()).isTrue();
        assertThat(copiaNpc.getJogadorId()).isNull();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
