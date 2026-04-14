package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.VantagemPreRequisitoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.FichaAptidao;
import br.com.hydroom.rpg.fichacontrolador.model.FichaAtributo;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemPreRequisitoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para pré-requisitos polimórficos de VantagemConfig (Spec 023).
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Criação de pré-req tipo VANTAGEM (retrocompatibilidade)</li>
 *   <li>Criação de pré-req tipo RACA</li>
 *   <li>Criação de pré-req tipo CLASSE</li>
 *   <li>Criação de pré-req tipo ATRIBUTO com valorMinimo</li>
 *   <li>Criação de pré-req tipo NIVEL com valorMinimo</li>
 *   <li>Criação de pré-req tipo APTIDAO com valorMinimo</li>
 *   <li>Lógica OR dentro do mesmo tipo</li>
 *   <li>Lógica AND entre tipos diferentes</li>
 *   <li>409 ao deletar Raca usada como pré-req</li>
 *   <li>409 ao deletar Classe usada como pré-req</li>
 *   <li>409 ao deletar AtributoConfig usado como pré-req</li>
 *   <li>409 ao deletar AptidaoConfig usada como pré-req</li>
 *   <li>Validação: campo obrigatório ausente lança ValidationException</li>
 *   <li>Validação: entidade de outro jogo lança ValidationException</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("VantagemPreRequisito Polimórficos - Spec 023")
class VantagemPreRequisitoPolimorficosIntegrationTest {

    @Autowired
    private VantagemConfiguracaoService vantagemService;

    @Autowired
    private RacaConfiguracaoService racaService;

    @Autowired
    private ClasseConfiguracaoService classeService;

    @Autowired
    private AtributoConfiguracaoService atributoService;

    @Autowired
    private AptidaoConfiguracaoService aptidaoService;

    @Autowired
    private VantagemConfigRepository vantagemRepository;

    @Autowired
    private VantagemPreRequisitoRepository prerequisitoRepository;

    @Autowired
    private ConfiguracaoRacaRepository racaRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoRepository;

    @Autowired
    private TipoAptidaoRepository tipoAptidaoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Jogo jogo;
    private TipoAptidao tipoAptidao;

    @BeforeEach
    void setUp() {
        prerequisitoRepository.deleteAll();
        vantagemRepository.deleteAll();
        aptidaoRepository.deleteAll();
        tipoAptidaoRepository.deleteAll();
        atributoRepository.deleteAll();
        racaRepository.deleteAll();
        classeRepository.deleteAll();
        jogoRepository.deleteAll();

        int n = counter.getAndIncrement();
        jogo = jogoRepository.save(Jogo.builder()
            .nome("Jogo Spec023 " + n)
            .jogoAtivo(true)
            .dataInicio(LocalDate.now())
            .build());

        tipoAptidao = tipoAptidaoRepository.save(TipoAptidao.builder()
            .jogo(jogo)
            .nome("Tipo " + n)
            .build());
    }

    // ===== HELPERS =====

    private VantagemConfig criarVantagem(String nome) {
        return vantagemService.criar(VantagemConfig.builder()
            .jogo(jogo)
            .nome(nome)
            .nivelMaximo(5)
            .formulaCusto("custo_base")
            .build());
    }

    private Raca criarRaca(String nome) {
        return racaRepository.save(Raca.builder().jogo(jogo).nome(nome).build());
    }

    private ClassePersonagem criarClasse(String nome) {
        return classeRepository.save(ClassePersonagem.builder().jogo(jogo).nome(nome).build());
    }

    private AtributoConfig criarAtributo(String nome, String abrev) {
        return atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo).nome(nome).abreviacao(abrev).build());
    }

    private AptidaoConfig criarAptidao(String nome) {
        return aptidaoRepository.save(AptidaoConfig.builder()
            .jogo(jogo).tipoAptidao(tipoAptidao).nome(nome).build());
    }

    // ===== TESTES: CRIAÇÃO DE TIPOS =====

    @Test
    @DisplayName("deve criar pré-req tipo VANTAGEM via novo request polimórfico")
    void deveCriarPreRequisitoTipoVantagem() {
        VantagemConfig a = criarVantagem("Vantagem A");
        VantagemConfig b = criarVantagem("Vantagem B");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(a.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.VANTAGEM, b.getId(), 2, null, null, null, null, null));

        assertThat(pr.getId()).isNotNull();
        assertThat(pr.getTipo()).isEqualTo(TipoPreRequisito.VANTAGEM);
        assertThat(pr.getRequisito().getId()).isEqualTo(b.getId());
        assertThat(pr.getNivelMinimo()).isEqualTo(2);
    }

    @Test
    @DisplayName("deve criar pré-req tipo RACA")
    void deveCriarPreRequisitoTipoRaca() {
        VantagemConfig v = criarVantagem("Vantagem Elfo");
        Raca elfo = criarRaca("Elfo");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.RACA, null, null, elfo.getId(), null, null, null, null));

        assertThat(pr.getId()).isNotNull();
        assertThat(pr.getTipo()).isEqualTo(TipoPreRequisito.RACA);
        assertThat(pr.getRaca().getId()).isEqualTo(elfo.getId());
    }

    @Test
    @DisplayName("deve criar pré-req tipo CLASSE")
    void deveCriarPreRequisitoTipoClasse() {
        VantagemConfig v = criarVantagem("Vantagem Mago");
        ClassePersonagem mago = criarClasse("Mago");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.CLASSE, null, null, null, mago.getId(), null, null, null));

        assertThat(pr.getTipo()).isEqualTo(TipoPreRequisito.CLASSE);
        assertThat(pr.getClasse().getId()).isEqualTo(mago.getId());
    }

    @Test
    @DisplayName("deve criar pré-req tipo ATRIBUTO com valorMinimo")
    void deveCriarPreRequisitoTipoAtributo() {
        VantagemConfig v = criarVantagem("Vantagem Forte");
        AtributoConfig forca = criarAtributo("Força", "FOR");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.ATRIBUTO, null, null, null, null, forca.getId(), null, 10));

        assertThat(pr.getTipo()).isEqualTo(TipoPreRequisito.ATRIBUTO);
        assertThat(pr.getAtributo().getId()).isEqualTo(forca.getId());
        assertThat(pr.getValorMinimo()).isEqualTo(10);
    }

    @Test
    @DisplayName("deve criar pré-req tipo NIVEL com valorMinimo")
    void deveCriarPreRequisitoTipoNivel() {
        VantagemConfig v = criarVantagem("Vantagem Veterano");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.NIVEL, null, null, null, null, null, null, 5));

        assertThat(pr.getTipo()).isEqualTo(TipoPreRequisito.NIVEL);
        assertThat(pr.getValorMinimo()).isEqualTo(5);
        assertThat(pr.getRaca()).isNull();
        assertThat(pr.getClasse()).isNull();
        assertThat(pr.getAtributo()).isNull();
    }

    @Test
    @DisplayName("deve criar pré-req tipo APTIDAO com valorMinimo")
    void deveCriarPreRequisitoTipoAptidao() {
        VantagemConfig v = criarVantagem("Vantagem Espadachim");
        AptidaoConfig espada = criarAptidao("Espadas");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.APTIDAO, null, null, null, null, null, espada.getId(), 3));

        assertThat(pr.getTipo()).isEqualTo(TipoPreRequisito.APTIDAO);
        assertThat(pr.getAptidao().getId()).isEqualTo(espada.getId());
        assertThat(pr.getValorMinimo()).isEqualTo(3);
    }

    // ===== TESTES: VALIDAÇÕES =====

    @Test
    @DisplayName("deve lançar ValidationException quando requisitoId ausente em tipo VANTAGEM")
    void deveLancarExcecaoSeRequisitoIdAusenteParaVantagem() {
        VantagemConfig v = criarVantagem("Vantagem Req Ausente");

        assertThrows(ValidationException.class, () ->
            vantagemService.adicionarPreRequisito(v.getId(),
                new VantagemPreRequisitoRequest(TipoPreRequisito.VANTAGEM, null, null, null, null, null, null, null)));
    }

    @Test
    @DisplayName("deve lançar ValidationException quando valorMinimo ausente em tipo ATRIBUTO")
    void deveLancarExcecaoSeValorMinimoAusenteParaAtributo() {
        VantagemConfig v = criarVantagem("Vantagem Atrib Sem Min");
        AtributoConfig forca = criarAtributo("Força S", "FRS");

        assertThrows(ValidationException.class, () ->
            vantagemService.adicionarPreRequisito(v.getId(),
                new VantagemPreRequisitoRequest(TipoPreRequisito.ATRIBUTO, null, null, null, null, forca.getId(), null, null)));
    }

    @Test
    @DisplayName("deve lançar ValidationException quando valorMinimo ausente em tipo NIVEL")
    void deveLancarExcecaoSeValorMinimoAusenteParaNivel() {
        VantagemConfig v = criarVantagem("Vantagem Nivel Sem Min");

        assertThrows(ValidationException.class, () ->
            vantagemService.adicionarPreRequisito(v.getId(),
                new VantagemPreRequisitoRequest(TipoPreRequisito.NIVEL, null, null, null, null, null, null, null)));
    }

    @Test
    @DisplayName("deve lançar ValidationException ao usar Raca de outro jogo")
    void deveLancarExcecaoParaRacaDeOutroJogo() {
        Jogo outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Outro Jogo Raca " + counter.getAndIncrement())
            .jogoAtivo(true).dataInicio(LocalDate.now()).build());
        Raca racaOutroJogo = racaRepository.save(Raca.builder().jogo(outroJogo).nome("Orc").build());
        VantagemConfig v = criarVantagem("Vantagem Cross Jogo");

        assertThrows(ValidationException.class, () ->
            vantagemService.adicionarPreRequisito(v.getId(),
                new VantagemPreRequisitoRequest(TipoPreRequisito.RACA, null, null, racaOutroJogo.getId(), null, null, null, null)));
    }

    // ===== TESTES: BLOQUEIO DE DELEÇÃO (409) =====

    @Test
    @DisplayName("deve lançar ConflictException ao deletar Raca usada como pré-req")
    void deveLancarConflictAoDeletarRacaUsadaComoPreReq() {
        Raca anao = criarRaca("Anão");
        VantagemConfig v = criarVantagem("Vantagem Anão");
        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.RACA, null, null, anao.getId(), null, null, null, null));

        assertThrows(ConflictException.class, () -> racaService.deletar(anao.getId()));
    }

    @Test
    @DisplayName("deve lançar ConflictException ao deletar Classe usada como pré-req")
    void deveLancarConflictAoDeletarClasseUsadaComoPreReq() {
        ClassePersonagem guerreiro = criarClasse("Guerreiro");
        VantagemConfig v = criarVantagem("Vantagem Guerreiro");
        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.CLASSE, null, null, null, guerreiro.getId(), null, null, null));

        assertThrows(ConflictException.class, () -> classeService.deletar(guerreiro.getId()));
    }

    @Test
    @DisplayName("deve lançar ConflictException ao deletar AtributoConfig usado como pré-req")
    void deveLancarConflictAoDeletarAtributoUsadoComoPreReq() {
        AtributoConfig agi = criarAtributo("Agilidade", "AGI");
        VantagemConfig v = criarVantagem("Vantagem Ágil");
        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.ATRIBUTO, null, null, null, null, agi.getId(), null, 8));

        assertThrows(ConflictException.class, () -> atributoService.deletar(agi.getId()));
    }

    @Test
    @DisplayName("deve lançar ConflictException ao deletar AptidaoConfig usada como pré-req")
    void deveLancarConflictAoDeletarAptidaoUsadaComoPreReq() {
        AptidaoConfig arcos = criarAptidao("Arcos");
        VantagemConfig v = criarVantagem("Vantagem Arqueiro");
        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.APTIDAO, null, null, null, null, null, arcos.getId(), 5));

        assertThrows(ConflictException.class, () -> aptidaoService.deletar(arcos.getId()));
    }

    @Test
    @DisplayName("deve permitir deletar Raca que NÃO é pré-req (sem lançar exceção)")
    void devePermitirDeletarRacaSemPreReq() {
        Raca humano = criarRaca("Humano");

        // Não deve lançar ConflictException
        racaService.deletar(humano.getId());

        // Verifica que a raça NÃO é mais listada (soft delete via SQLRestriction)
        List<Raca> racasVisiveis = racaRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        assertThat(racasVisiveis).noneMatch(r -> r.getId().equals(humano.getId()));
    }

    // ===== TESTES: LÓGICA OR/AND =====

    @Test
    @DisplayName("deve listar pré-req dos novos tipos junto com tipo VANTAGEM")
    void deveListarPreRequisitosMistos() {
        VantagemConfig v = criarVantagem("Vantagem Mista");
        VantagemConfig outravantagem = criarVantagem("Outra");
        Raca elfo = criarRaca("Elfo Lista");
        AtributoConfig forca = criarAtributo("Força Lista", "FRL");

        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.VANTAGEM, outravantagem.getId(), 1, null, null, null, null, null));
        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.RACA, null, null, elfo.getId(), null, null, null, null));
        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.ATRIBUTO, null, null, null, null, forca.getId(), null, 10));

        List<VantagemPreRequisito> lista = vantagemService.listarPreRequisitos(v.getId());

        assertThat(lista).hasSize(3);
        assertThat(lista).extracting(VantagemPreRequisito::getTipo)
            .containsExactlyInAnyOrder(TipoPreRequisito.VANTAGEM, TipoPreRequisito.RACA, TipoPreRequisito.ATRIBUTO);
    }

    @Test
    @DisplayName("deve aceitar dois pré-req do mesmo tipo RACA (lógica OR permitida)")
    void deveAceitarDoisPreReqDoMesmoTipo() {
        VantagemConfig v = criarVantagem("Vantagem Multi-Raca");
        Raca elfo = criarRaca("Elfo OR");
        Raca anao = criarRaca("Anão OR");

        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.RACA, null, null, elfo.getId(), null, null, null, null));
        vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.RACA, null, null, anao.getId(), null, null, null, null));

        List<VantagemPreRequisito> lista = vantagemService.listarPreRequisitos(v.getId());
        assertThat(lista).hasSize(2);
        assertThat(lista).allMatch(pr -> pr.getTipo() == TipoPreRequisito.RACA);
    }

    @Test
    @DisplayName("deve remover pré-req polimórfico com sucesso")
    void deveRemoverPreRequisitoPolimórfico() {
        VantagemConfig v = criarVantagem("Vantagem Removivel");
        Raca elfo = criarRaca("Elfo Rem");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(v.getId(),
            new VantagemPreRequisitoRequest(TipoPreRequisito.RACA, null, null, elfo.getId(), null, null, null, null));

        vantagemService.removerPreRequisito(pr.getId());

        assertThat(vantagemService.listarPreRequisitos(v.getId())).isEmpty();
    }
}
