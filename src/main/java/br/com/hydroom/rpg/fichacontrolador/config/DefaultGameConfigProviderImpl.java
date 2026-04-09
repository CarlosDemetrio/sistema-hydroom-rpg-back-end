package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.config.defaults.*;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Thin facade que delega para providers especializados no pacote {@code config.defaults}.
 *
 * <p>Fornece todos os dados default para inicialização de um jogo novo.</p>
 * <p>Valores são baseados nas regras canônicas do sistema Klayrah RPG.</p>
 *
 * <p><strong>Para customizar</strong>: Crie sua própria implementação de GameDefaultConfigProvider
 * e registre como @Primary no Spring Context.</p>
 *
 * @author Carlos Demétrio
 * @since 2026-02-05
 */
@Component
@RequiredArgsConstructor
public class DefaultGameConfigProviderImpl implements GameDefaultConfigProvider {

    private final DefaultAtributosProvider atributosProvider;
    private final DefaultAptidoesProvider aptidoesProvider;
    private final DefaultNiveisProvider niveisProvider;
    private final DefaultClassesProvider classesProvider;
    private final DefaultRacasProvider racasProvider;
    private final DefaultProspeccoesProvider prospeccoesProvider;
    private final DefaultConfigSimpleProvider simpleProvider;
    private final DefaultBonusProvider bonusProvider;
    private final DefaultPontosVantagemProvider pontosVantagemProvider;
    private final DefaultVantagensProvider vantagensProvider;
    private final DefaultItensProvider itensProvider;


    @Override
    public List<AtributoConfigDTO> getDefaultAtributos() {
        return atributosProvider.get();
    }

    @Override
    public List<AptidaoConfigDTO> getDefaultAptidoes() {
        return aptidoesProvider.get();
    }

    @Override
    public List<NivelConfigDTO> getDefaultNiveis() {
        return niveisProvider.getNiveis();
    }

    @Override
    public List<LimitadorConfigDTO> getDefaultLimitadores() {
        return niveisProvider.getLimitadores();
    }

    @Override
    public List<ClasseConfigDTO> getDefaultClasses() {
        return classesProvider.get();
    }

    @Override
    public List<RacaConfigDTO> getDefaultRacas() {
        return racasProvider.getRacas();
    }

    @Override
    public Map<String, List<BonusAtributoDTO>> getDefaultBonusRaciais() {
        return racasProvider.getBonusRaciais();
    }

    @Override
    public List<ProspeccaoConfigDTO> getDefaultProspeccoes() {
        return prospeccoesProvider.get();
    }

    @Override
    public List<GeneroConfigDTO> getDefaultGeneros() {
        return simpleProvider.getGeneros();
    }

    @Override
    public List<IndoleConfigDTO> getDefaultIndoles() {
        return simpleProvider.getIndoles();
    }

    @Override
    public List<PresencaConfigDTO> getDefaultPresencas() {
        return simpleProvider.getPresencas();
    }

    @Override
    public List<MembroCorpoConfigDTO> getDefaultMembrosCorpo() {
        return simpleProvider.getMembrosCorpo();
    }

    @Override
    public List<VantagemConfigDTO> getDefaultVantagens() {
        return vantagensProvider.getVantagens();
    }

    @Override
    public List<BonusConfigDTO> getDefaultBonus() {
        return bonusProvider.get();
    }

    @Override
    public List<PontosVantagemConfigDTO> getDefaultPontosVantagem() {
        return pontosVantagemProvider.get();
    }

    @Override
    public List<CategoriaVantagemDTO> getDefaultCategoriasVantagem() {
        return vantagensProvider.getCategorias();
    }

    @Override
    public List<RaridadeItemConfigDefault> getDefaultRaridades() {
        return itensProvider.getRaridades();
    }

    @Override
    public List<TipoItemConfigDefault> getDefaultTipos() {
        return itensProvider.getTipos();
    }

    @Override
    public List<ItemConfigDefault> getDefaultItens() {
        return itensProvider.getItens();
    }
}
