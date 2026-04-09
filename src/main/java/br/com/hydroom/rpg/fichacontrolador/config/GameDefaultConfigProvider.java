package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;

import java.util.List;
import java.util.Map;

/**
 * Provider de configurações padrão para inicialização de jogos.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Prover valores padrão para todas as tabelas de configuração</li>
 *   <li>Valores são configuráveis via código (não SQL)</li>
 *   <li>Fácil manutenção e customização dos defaults</li>
 * </ul>
 *
 * <p>Implementação padrão: {@code DefaultGameConfigProviderImpl}</p>
 *
 * @author Carlos Demétrio
 * @see DefaultGameConfigProviderImpl
 * @since 2026-02-05
 */
public interface GameDefaultConfigProvider {

    /**
     * Retorna os 7 atributos padrão.
     * Exemplo: Força, Agilidade, Vigor, Sabedoria, Intuição, Inteligência, Astúcia
     *
     * @return Lista de AtributoConfigDTO com fórmulas de ímpeto
     */
    List<AtributoConfigDTO> getDefaultAtributos();

    /**
     * Retorna as 24 aptidões padrão (12 físicas + 12 mentais).
     *
     * @return Lista de AptidaoConfigDTO
     */
    List<AptidaoConfigDTO> getDefaultAptidoes();

    /**
     * Retorna os 36 níveis padrão (nível 0 até 35).
     * Cada nível tem: XP necessária, pontos de atributo, pontos de vantagem, pontos de aptidão.
     *
     * @return Lista de NivelConfigDTO
     */
    List<NivelConfigDTO> getDefaultNiveis();

    /**
     * Retorna os 5 limitadores de atributo por faixa de nível.
     *
     * @return Lista de LimitadorConfigDTO
     */
    List<LimitadorConfigDTO> getDefaultLimitadores();

    /**
     * Retorna as 12 classes padrão.
     * Exemplo: Guerreiro, Arqueiro, Monge, Mago, etc.
     *
     * @return Lista de ClasseConfigDTO
     */
    List<ClasseConfigDTO> getDefaultClasses();

    /**
     * Retorna as 6 raças padrão.
     * Exemplo: Humano, Karzarcryer, Ikaruz, Hankraz, Atlas, Anakarys
     *
     * @return Lista de RacaConfigDTO
     */
    List<RacaConfigDTO> getDefaultRacas();

    /**
     * Retorna os bônus raciais de atributos.
     * Mapa com chave = nome da raça, valor = lista de bônus de atributos.
     *
     * Exemplo:
     * <pre>
     * {
     *   "Anakarys": [BonusAtributoDTO("AGI", +3), BonusAtributoDTO("INTU", +6)],
     *   "Ikaruz":   [BonusAtributoDTO("SAB", +5), BonusAtributoDTO("VIG", -9)]
     * }
     * </pre>
     *
     * @return Mapa de bônus raciais
     */
    Map<String, List<BonusAtributoDTO>> getDefaultBonusRaciais();

    /**
     * Retorna os 6 dados de prospecção padrão.
     * Exemplo: d3, d4, d6, d8, d10, d12
     *
     * @return Lista de ProspeccaoConfigDTO
     */
    List<ProspeccaoConfigDTO> getDefaultProspeccoes();

    /**
     * Retorna os 3 gêneros padrão.
     * Exemplo: Masculino, Feminino, Outro
     *
     * @return Lista de GeneroConfigDTO
     */
    List<GeneroConfigDTO> getDefaultGeneros();

    /**
     * Retorna as 3 índoles padrão.
     * Exemplo: Bondoso, Neutro, Maligno
     *
     * @return Lista de IndoleConfigDTO
     */
    List<IndoleConfigDTO> getDefaultIndoles();

    /**
     * Retorna os 4 níveis de presença padrão.
     * Exemplo: Baixa, Moderada, Alta, Dominante
     *
     * @return Lista de PresencaConfigDTO
     */
    List<PresencaConfigDTO> getDefaultPresencas();

    /**
     * Retorna os 6 membros do corpo padrão.
     * Exemplo: Cabeça, Tronco, Braço Direito, Braço Esquerdo, Perna Direita, Perna Esquerda
     *
     * @return Lista de MembroCorpoConfigDTO
     */
    List<MembroCorpoConfigDTO> getDefaultMembrosCorpo();

    /**
     * Retorna vantagens de exemplo (opcional).
     * Pode retornar lista vazia se o mestre preferir criar suas próprias vantagens.
     *
     * @return Lista de VantagemConfigDTO
     */
    List<VantagemConfigDTO> getDefaultVantagens();

    /**
     * Retorna os 9 bônus calculados padrão (B.B.A, B.B.M, Defesa, Esquiva, etc.).
     *
     * @return Lista de BonusConfigDTO com fórmulas
     */
    List<BonusConfigDTO> getDefaultBonus();

    /**
     * Retorna os 8 marcos de pontos de vantagem por nível.
     * Cada entrada indica quantos pontos de vantagem são ganhos ao atingir aquele nível.
     *
     * @return Lista de PontosVantagemConfigDTO
     */
    List<PontosVantagemConfigDTO> getDefaultPontosVantagem();

    /**
     * Retorna as 8 categorias de vantagem padrão com cores hex.
     *
     * @return Lista de CategoriaVantagemDTO
     */
    List<CategoriaVantagemDTO> getDefaultCategoriasVantagem();

    /**
     * Retorna defaults de pontos extras por classe (por nível).
     *
     * <p>TODO PA-015-01: Definir com PO os valores canônicos por classe.
     * Por enquanto retorna vazio — Mestre configura manualmente após criar o jogo.</p>
     *
     * @return Mapa de nome da classe → lista de pontos por nível
     */
    Map<String, List<?>> getDefaultClassePontos();

    /**
     * Retorna defaults de pontos extras por raça (por nível).
     *
     * <p>TODO PA-015-02: Definir com PO os valores canônicos por raça.
     * Por enquanto retorna vazio — Mestre configura manualmente após criar o jogo.</p>
     *
     * @return Mapa de nome da raça → lista de pontos por nível
     */
    Map<String, List<?>> getDefaultRacaPontos();

    /**
     * Retorna as 7 raridades de itens padrão (Comum → Único).
     *
     * @return Lista de RaridadeItemConfigDefault com cores e limites de bônus
     */
    List<RaridadeItemConfigDefault> getDefaultRaridades();

    /**
     * Retorna os 20 tipos de itens padrão (Armas, Armaduras, Acessórios, Consumíveis, Aventura).
     *
     * @return Lista de TipoItemConfigDefault com categoria e subcategoria
     */
    List<TipoItemConfigDefault> getDefaultTipos();

    /**
     * Retorna os 40 itens SRD adaptados ao sistema Klayrah.
     * Inclui: 15 armas, 10 armaduras/escudos, 5 acessórios, 5 consumíveis, 5 aventura.
     *
     * @return Lista de ItemConfigDefault com efeitos inline
     */
    List<ItemConfigDefault> getDefaultItens();
}
