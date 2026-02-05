package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciar todas as configurações do sistema.
 * IMPORTANTE: Todas as configurações são POR JOGO (cada mestre pode ter suas próprias configs).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoAtributoRepository atributoRepository;
    private final ConfiguracaoAptidaoRepository aptidaoRepository;
    private final ConfiguracaoClasseRepository classeRepository;
    private final ConfiguracaoRacaRepository racaRepository;
    private final DadoProspeccaoConfigRepository dadoProspeccaoRepository;
    private final VantagemConfigRepository vantagemRepository;
    private final GeneroConfigRepository generoRepository;
    private final IndoleConfigRepository indoleRepository;
    private final PresencaConfigRepository presencaRepository;
    private final MembroCorpoConfigRepository membroCorpoRepository;
    private final ConfiguracaoNivelRepository nivelRepository;
    private final BonusConfigRepository bonusRepository;
    private final TipoAptidaoRepository tipoAptidaoRepository;

    // ===================================================================
    // ATRIBUTOS
    // ===================================================================

    public List<AtributoConfig> listarAtributos(Long jogoId) {
        return atributoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public AtributoConfig buscarAtributo(Long id) {
        return atributoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atributo não encontrado: " + id));
    }

    // ===================================================================
    // APTIDÕES
    // ===================================================================

    public List<AptidaoConfig> listarAptidoes(Long jogoId) {
        return aptidaoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public List<AptidaoConfig> listarAptidoesPorTipo(Long jogoId, TipoAptidao tipoAptidao) {
        return aptidaoRepository.findByJogoIdAndTipoAptidaoAndAtivoTrueOrderByOrdemExibicao(jogoId, tipoAptidao);
    }

    public AptidaoConfig buscarAptidao(Long id) {
        return aptidaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aptidão não encontrada: " + id));
    }

    // ===================================================================
    // CLASSES
    // ===================================================================

    public List<ClassePersonagem> listarClasses(Long jogoId) {
        return classeRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public ClassePersonagem buscarClasse(Long id) {
        return classeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classe não encontrada: " + id));
    }

    // ===================================================================
    // RAÇAS
    // ===================================================================

    public List<Raca> listarRacas(Long jogoId) {
        return racaRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public Raca buscarRaca(Long id) {
        return racaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Raça não encontrada: " + id));
    }

    // ===================================================================
    // DADOS DE PROSPECÇÃO
    // ===================================================================

    public List<DadoProspeccaoConfig> listarDadosProspeccao(Long jogoId) {
        return dadoProspeccaoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public DadoProspeccaoConfig buscarDadoProspeccao(Long id) {
        return dadoProspeccaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dado de prospecção não encontrado: " + id));
    }

    // ===================================================================
    // VANTAGENS
    // ===================================================================

    public List<VantagemConfig> listarVantagens(Long jogoId) {
        return vantagemRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public VantagemConfig buscarVantagem(Long id) {
        return vantagemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vantagem não encontrada: " + id));
    }

    // ===================================================================
    // ATRIBUTOS - CREATE, UPDATE, DELETE
    // ===================================================================

    @Transactional
    public AtributoConfig criarAtributo(AtributoConfig atributo) {
        atributo.restore();
        return atributoRepository.save(atributo);
    }

    @Transactional
    public AtributoConfig atualizarAtributo(Long id, AtributoConfig atributoAtualizado) {
        AtributoConfig atributo = atributoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atributo não encontrado: " + id));

        atributo.setNome(atributoAtualizado.getNome());
        atributo.setDescricao(atributoAtualizado.getDescricao());
        atributo.setOrdemExibicao(atributoAtualizado.getOrdemExibicao());
        atributo.setFormulaImpeto(atributoAtualizado.getFormulaImpeto());

        return atributoRepository.save(atributo);
    }

    @Transactional
    public void deletarAtributo(Long id) {
        AtributoConfig atributo = atributoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atributo não encontrado: " + id));

        atributo.delete();
        atributoRepository.save(atributo);
    }

    // ===================================================================
    // APTIDÕES - CREATE, UPDATE, DELETE
    // ===================================================================

    @Transactional
    public AptidaoConfig criarAptidao(AptidaoConfig aptidao) {
        aptidao.restore();
        return aptidaoRepository.save(aptidao);
    }

    @Transactional
    public AptidaoConfig atualizarAptidao(Long id, AptidaoConfig aptidaoAtualizada) {
        AptidaoConfig aptidao = aptidaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aptidão não encontrada: " + id));

        aptidao.setNome(aptidaoAtualizada.getNome());
        aptidao.setDescricao(aptidaoAtualizada.getDescricao());
        aptidao.setOrdemExibicao(aptidaoAtualizada.getOrdemExibicao());
        aptidao.setTipoAptidao(aptidaoAtualizada.getTipoAptidao());

        return aptidaoRepository.save(aptidao);
    }

    @Transactional
    public void deletarAptidao(Long id) {
        AptidaoConfig aptidao = aptidaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aptidão não encontrada: " + id));

        aptidao.delete();
        aptidaoRepository.save(aptidao);
    }

    // ===================================================================
    // CLASSES - CREATE, UPDATE, DELETE
    // ===================================================================

    @Transactional
    public ClassePersonagem criarClasse(ClassePersonagem classe) {
        classe.restore();
        return classeRepository.save(classe);
    }

    @Transactional
    public ClassePersonagem atualizarClasse(Long id, ClassePersonagem classeAtualizada) {
        ClassePersonagem classe = classeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classe não encontrada: " + id));

        classe.setNome(classeAtualizada.getNome());
        classe.setDescricao(classeAtualizada.getDescricao());
        classe.setOrdemExibicao(classeAtualizada.getOrdemExibicao());

        return classeRepository.save(classe);
    }

    @Transactional
    public void deletarClasse(Long id) {
        ClassePersonagem classe = classeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classe não encontrada: " + id));

        classe.delete();
        classeRepository.save(classe);
    }

    // ===================================================================
    // RAÇAS - CREATE, UPDATE, DELETE
    // ===================================================================

    @Transactional
    public Raca criarRaca(Raca raca) {
        raca.restore();
        return racaRepository.save(raca);
    }

    @Transactional
    public Raca atualizarRaca(Long id, Raca racaAtualizada) {
        Raca raca = racaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Raça não encontrada: " + id));

        raca.setNome(racaAtualizada.getNome());
        raca.setDescricao(racaAtualizada.getDescricao());
        raca.setOrdemExibicao(racaAtualizada.getOrdemExibicao());

        return racaRepository.save(raca);
    }

    @Transactional
    public void deletarRaca(Long id) {
        Raca raca = racaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Raça não encontrada: " + id));

        raca.delete();
        racaRepository.save(raca);
    }

    // ===================================================================
    // VANTAGENS - CREATE, UPDATE, DELETE
    // ===================================================================

    @Transactional
    public VantagemConfig criarVantagem(VantagemConfig vantagem) {
        vantagem.restore();
        return vantagemRepository.save(vantagem);
    }

    @Transactional
    public VantagemConfig atualizarVantagem(Long id, VantagemConfig vantagemAtualizada) {
        VantagemConfig vantagem = vantagemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vantagem não encontrada: " + id));

        vantagem.setNome(vantagemAtualizada.getNome());
        vantagem.setDescricao(vantagemAtualizada.getDescricao());
        vantagem.setOrdemExibicao(vantagemAtualizada.getOrdemExibicao());
        vantagem.setNivelMaximo(vantagemAtualizada.getNivelMaximo());
        vantagem.setFormulaCusto(vantagemAtualizada.getFormulaCusto());
        vantagem.setDescricaoEfeito(vantagemAtualizada.getDescricaoEfeito());

        return vantagemRepository.save(vantagem);
    }

    @Transactional
    public void deletarVantagem(Long id) {
        VantagemConfig vantagem = vantagemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vantagem não encontrada: " + id));

        vantagem.delete();
        vantagemRepository.save(vantagem);
    }

    // ===================================================================
    // DADOS DE PROSPECÇÃO - CREATE, UPDATE, DELETE
    // ===================================================================

    @Transactional
    public DadoProspeccaoConfig criarDadoProspeccao(DadoProspeccaoConfig dado) {
        dado.restore();
        return dadoProspeccaoRepository.save(dado);
    }

    @Transactional
    public DadoProspeccaoConfig atualizarDadoProspeccao(Long id, DadoProspeccaoConfig dadoAtualizado) {
        DadoProspeccaoConfig dado = dadoProspeccaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dado de prospecção não encontrado: " + id));

        dado.setNome(dadoAtualizado.getNome());
        dado.setDescricao(dadoAtualizado.getDescricao());
        dado.setNumeroFaces(dadoAtualizado.getNumeroFaces());
        dado.setOrdemExibicao(dadoAtualizado.getOrdemExibicao());

        return dadoProspeccaoRepository.save(dado);
    }

    @Transactional
    public void deletarDadoProspeccao(Long id) {
        DadoProspeccaoConfig dado = dadoProspeccaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dado de prospecção não encontrado: " + id));

        dado.delete();
        dadoProspeccaoRepository.save(dado);
    }

    // ===================================================================
    // GÊNEROS - READ (já existe), CREATE, UPDATE, DELETE
    // ===================================================================

    public List<GeneroConfig> listarGeneros(Long jogoId) {
        return generoRepository.findByJogoIdAndAtivoTrueOrderByOrdem(jogoId);
    }

    public GeneroConfig buscarGenero(Long id) {
        return generoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gênero não encontrado: " + id));
    }

    @Transactional
    public GeneroConfig criarGenero(GeneroConfig genero) {
        genero.restore();
        return generoRepository.save(genero);
    }

    @Transactional
    public GeneroConfig atualizarGenero(Long id, GeneroConfig generoAtualizado) {
        GeneroConfig genero = generoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gênero não encontrado: " + id));

        genero.setNome(generoAtualizado.getNome());
        genero.setDescricao(generoAtualizado.getDescricao());
        genero.setOrdem(generoAtualizado.getOrdem());

        return generoRepository.save(genero);
    }

    @Transactional
    public void deletarGenero(Long id) {
        GeneroConfig genero = generoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gênero não encontrado: " + id));

        genero.delete();
        generoRepository.save(genero);
    }

    // ===================================================================
    // INDOLES - READ, CREATE, UPDATE, DELETE
    // ===================================================================

    public List<IndoleConfig> listarIndoles(Long jogoId) {
        return indoleRepository.findByJogoIdAndAtivoTrueOrderByOrdem(jogoId);
    }

    public IndoleConfig buscarIndole(Long id) {
        return indoleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Indole não encontrada: " + id));
    }

    @Transactional
    public IndoleConfig criarIndole(IndoleConfig indole) {
        indole.restore();
        return indoleRepository.save(indole);
    }

    @Transactional
    public IndoleConfig atualizarIndole(Long id, IndoleConfig indoleAtualizada) {
        IndoleConfig indole = indoleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Indole não encontrada: " + id));

        indole.setNome(indoleAtualizada.getNome());
        indole.setDescricao(indoleAtualizada.getDescricao());
        indole.setOrdem(indoleAtualizada.getOrdem());

        return indoleRepository.save(indole);
    }

    @Transactional
    public void deletarIndole(Long id) {
        IndoleConfig indole = indoleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Indole não encontrada: " + id));

        indole.delete();
        indoleRepository.save(indole);
    }

    // ===================================================================
    // PRESENÇAS - READ, CREATE, UPDATE, DELETE
    // ===================================================================

    public List<PresencaConfig> listarPresencas(Long jogoId) {
        return presencaRepository.findByJogoIdAndAtivoTrueOrderByOrdem(jogoId);
    }

    public PresencaConfig buscarPresenca(Long id) {
        return presencaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presença não encontrada: " + id));
    }

    @Transactional
    public PresencaConfig criarPresenca(PresencaConfig presenca) {
        presenca.restore();
        return presencaRepository.save(presenca);
    }

    @Transactional
    public PresencaConfig atualizarPresenca(Long id, PresencaConfig presencaAtualizada) {
        PresencaConfig presenca = presencaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presença não encontrada: " + id));

        presenca.setNome(presencaAtualizada.getNome());
        presenca.setDescricao(presencaAtualizada.getDescricao());
        presenca.setOrdem(presencaAtualizada.getOrdem());

        return presencaRepository.save(presenca);
    }

    @Transactional
    public void deletarPresenca(Long id) {
        PresencaConfig presenca = presencaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presença não encontrada: " + id));

        presenca.delete();
        presencaRepository.save(presenca);
    }

    // ===================================================================
    // MEMBROS DO CORPO - READ, CREATE, UPDATE, DELETE
    // ===================================================================

    public List<MembroCorpoConfig> listarMembrosCorpo(Long jogoId) {
        return membroCorpoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public MembroCorpoConfig buscarMembroCorpo(Long id) {
        return membroCorpoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro do corpo não encontrado: " + id));
    }

    @Transactional
    public MembroCorpoConfig criarMembroCorpo(MembroCorpoConfig membro) {
        membro.restore();
        return membroCorpoRepository.save(membro);
    }

    @Transactional
    public MembroCorpoConfig atualizarMembroCorpo(Long id, MembroCorpoConfig membroAtualizado) {
        MembroCorpoConfig membro = membroCorpoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro do corpo não encontrado: " + id));

        membro.setNome(membroAtualizado.getNome());
        membro.setPorcentagemVida(membroAtualizado.getPorcentagemVida());
        membro.setOrdemExibicao(membroAtualizado.getOrdemExibicao());

        return membroCorpoRepository.save(membro);
    }

    @Transactional
    public void deletarMembroCorpo(Long id) {
        MembroCorpoConfig membro = membroCorpoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro do corpo não encontrado: " + id));

        membro.delete();
        membroCorpoRepository.save(membro);
    }

    // ===================================================================
    // NÍVEIS - READ, CREATE, UPDATE, DELETE
    // ===================================================================

    public List<NivelConfig> listarNiveis(Long jogoId) {
        return nivelRepository.findByJogoIdAndAtivoTrueOrderByNivel(jogoId);
    }

    public NivelConfig buscarNivel(Long id) {
        return nivelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nível não encontrado: " + id));
    }

    @Transactional
    public NivelConfig criarNivel(NivelConfig nivel) {
        nivel.restore();
        return nivelRepository.save(nivel);
    }

    @Transactional
    public NivelConfig atualizarNivel(Long id, NivelConfig nivelAtualizado) {
        NivelConfig nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nível não encontrado: " + id));

        nivel.setNivel(nivelAtualizado.getNivel());
        nivel.setXpNecessaria(nivelAtualizado.getXpNecessaria());
        nivel.setPontosAtributo(nivelAtualizado.getPontosAtributo());
        nivel.setLimitadorAtributo(nivelAtualizado.getLimitadorAtributo());

        return nivelRepository.save(nivel);
    }

    @Transactional
    public void deletarNivel(Long id) {
        NivelConfig nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nível não encontrado: " + id));

        nivel.delete();
        nivelRepository.save(nivel);
    }

    // ===================================================================
    // BÔNUS - READ, CREATE, UPDATE, DELETE
    // ===================================================================

    public List<BonusConfig> listarBonus(Long jogoId) {
        return bonusRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public BonusConfig buscarBonus(Long id) {
        return bonusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bônus não encontrado: " + id));
    }

    @Transactional
    public BonusConfig criarBonus(BonusConfig bonus) {
        bonus.restore();
        return bonusRepository.save(bonus);
    }

    @Transactional
    public BonusConfig atualizarBonus(Long id, BonusConfig bonusAtualizado) {
        BonusConfig bonus = bonusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bônus não encontrado: " + id));

        bonus.setNome(bonusAtualizado.getNome());
        bonus.setDescricao(bonusAtualizado.getDescricao());
        bonus.setFormulaBase(bonusAtualizado.getFormulaBase());
        bonus.setOrdemExibicao(bonusAtualizado.getOrdemExibicao());

        return bonusRepository.save(bonus);
    }

    @Transactional
    public void deletarBonus(Long id) {
        BonusConfig bonus = bonusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bônus não encontrado: " + id));

        bonus.delete();
        bonusRepository.save(bonus);
    }

    // ===================================================================
    // TIPOS DE APTIDÃO - READ, CREATE, UPDATE, DELETE
    // ===================================================================

    public List<TipoAptidao> listarTiposAptidao(Long jogoId) {
        return tipoAptidaoRepository.findByJogoIdAndAtivoTrueOrderByOrdemExibicao(jogoId);
    }

    public TipoAptidao buscarTipoAptidao(Long id) {
        return tipoAptidaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de aptidão não encontrado: " + id));
    }

    @Transactional
    public TipoAptidao criarTipoAptidao(TipoAptidao tipoAptidao) {
        tipoAptidao.restore();
        return tipoAptidaoRepository.save(tipoAptidao);
    }

    @Transactional
    public TipoAptidao atualizarTipoAptidao(Long id, TipoAptidao tipoAptidaoAtualizado) {
        TipoAptidao tipoAptidao = tipoAptidaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de aptidão não encontrado: " + id));

        tipoAptidao.setNome(tipoAptidaoAtualizado.getNome());
        tipoAptidao.setDescricao(tipoAptidaoAtualizado.getDescricao());
        tipoAptidao.setOrdemExibicao(tipoAptidaoAtualizado.getOrdemExibicao());

        return tipoAptidaoRepository.save(tipoAptidao);
    }

    @Transactional
    public void deletarTipoAptidao(Long id) {
        TipoAptidao tipoAptidao = tipoAptidaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de aptidão não encontrado: " + id));

        tipoAptidao.delete();
        tipoAptidaoRepository.save(tipoAptidao);
    }
}
