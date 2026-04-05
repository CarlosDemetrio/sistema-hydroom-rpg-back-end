package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsável pelo cálculo de valores derivados da Ficha.
 *
 * <p>Recalcula: totais de atributos, ímpeto, base de bônus, vida, vida por membro,
 * essência e ameaça. Não persiste nada - apenas atualiza os campos das entidades
 * recebidas.</p>
 *
 * <p>Sequência de recálculo:</p>
 * <ol>
 *   <li>Reset de campos deriváveis (idempotência)</li>
 *   <li>Bônus raciais em FichaAtributo.outros (GAP-CALC-03)</li>
 *   <li>Bônus de classe em FichaBonus.classe (GAP-CALC-01)</li>
 *   <li>Bônus de classe em FichaAptidao.classe (GAP-CALC-02)</li>
 *   <li>Totais de atributos e ímpeto</li>
 *   <li>Totais de bônus derivados</li>
 *   <li>Vida, membros, essência, ameaça</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FichaCalculationService {

    private final FormulaEvaluatorService formulaEvaluatorService;

    // ==================== ATRIBUTOS ====================

    /**
     * Recalcula o total de um atributo: base + nivel + outros.
     */
    public int calcularTotalAtributo(FichaAtributo atributo) {
        atributo.recalcularTotal();
        return atributo.getTotal();
    }

    /**
     * Calcula o ímpeto de um atributo usando a fórmula do AtributoConfig.
     * Se fórmula nula ou em branco, retorna 0.0.
     */
    public double calcularImpeto(FichaAtributo atributo, AtributoConfig config) {
        String formula = config.getFormulaImpeto();
        if (formula == null || formula.isBlank()) {
            atributo.setImpeto(0.0);
            return 0.0;
        }
        try {
            double impeto = formulaEvaluatorService.calcularImpeto(formula, atributo.getTotal());
            atributo.setImpeto(impeto);
            return impeto;
        } catch (Exception e) {
            log.warn("Erro ao calcular ímpeto para atributo {}: {}", config.getNome(), e.getMessage());
            atributo.setImpeto(0.0);
            return 0.0;
        }
    }

    /**
     * Recalcula total e ímpeto de todos os atributos.
     */
    public void recalcularAtributos(List<FichaAtributo> atributos) {
        for (FichaAtributo atributo : atributos) {
            calcularTotalAtributo(atributo);
            if (atributo.getAtributoConfig() != null) {
                calcularImpeto(atributo, atributo.getAtributoConfig());
            }
        }
    }

    // ==================== BÔNUS ====================

    /**
     * Constrói mapa de variáveis para fórmulas de bônus.
     * Chave = abreviação do atributo, valor = total do atributo.
     */
    public Map<String, Integer> buildVariaveisAtributos(List<FichaAtributo> atributos) {
        Map<String, Integer> variaveis = new HashMap<>();
        for (FichaAtributo atributo : atributos) {
            if (atributo.getAtributoConfig() != null) {
                String abreviacao = atributo.getAtributoConfig().getAbreviacao();
                if (abreviacao != null && !abreviacao.isBlank()) {
                    variaveis.put(abreviacao, atributo.getTotal() != null ? atributo.getTotal() : 0);
                }
            }
        }
        return variaveis;
    }

    /**
     * Calcula o valor base de um bônus usando a fórmula do BonusConfig.
     * Se fórmula nula ou em branco, retorna 0.
     */
    public int calcularBaseBonus(FichaBonus fichaBonus, BonusConfig config, Map<String, Integer> variaveis) {
        String formula = config.getFormulaBase();
        if (formula == null || formula.isBlank()) {
            fichaBonus.setBase(0);
            return 0;
        }
        try {
            double resultado = formulaEvaluatorService.calcularDerivado(formula, variaveis);
            int base = (int) Math.round(resultado);
            fichaBonus.setBase(base);
            return base;
        } catch (Exception e) {
            log.warn("Erro ao calcular base de bônus {}: {}", config.getNome(), e.getMessage());
            fichaBonus.setBase(0);
            return 0;
        }
    }

    /**
     * Recalcula o total de um bônus: base + vantagens + classe + itens + gloria + outros.
     */
    public int calcularTotalBonus(FichaBonus fichaBonus) {
        fichaBonus.recalcularTotal();
        return fichaBonus.getTotal();
    }

    /**
     * Recalcula todos os bônus usando os atributos como variáveis para fórmulas.
     */
    public void recalcularBonus(List<FichaAtributo> atributos, List<FichaBonus> bonus) {
        Map<String, Integer> variaveis = buildVariaveisAtributos(atributos);
        for (FichaBonus fichaBonus : bonus) {
            if (fichaBonus.getBonusConfig() != null) {
                calcularBaseBonus(fichaBonus, fichaBonus.getBonusConfig(), variaveis);
            }
            calcularTotalBonus(fichaBonus);
        }
    }

    // ==================== VIDA / ESSÊNCIA / AMEAÇA ====================

    /**
     * Calcula a vida total da ficha.
     * Formula: vigorTotal + ficha.nivel + vida.vt + ficha.renascimentos + vida.outros
     *
     * <p>Nota: vida.vt representa pontos de vida adicionais por vantagens/raça/etc.
     * O vigor total do atributo é somado diretamente.</p>
     */
    public int calcularVidaTotal(Ficha ficha, FichaVida vida, int vigorTotal) {
        int nivelFicha = ficha.getNivel() != null ? ficha.getNivel() : 1;
        int renascimentos = ficha.getRenascimentos() != null ? ficha.getRenascimentos() : 0;
        int vt = vida.getVt() != null ? vida.getVt() : 0;
        int outros = vida.getOutros() != null ? vida.getOutros() : 0;

        int vidaTotal = vigorTotal + nivelFicha + vt + renascimentos + outros;
        vida.setVidaTotal(vidaTotal);

        // Inicializa vidaAtual com vidaTotal se ainda estiver zerada (nova ficha)
        if (vida.getVidaAtual() == null || vida.getVidaAtual() == 0) {
            vida.setVidaAtual(vidaTotal);
        }

        return vidaTotal;
    }

    /**
     * Calcula a vida de um membro do corpo: floor(vidaTotal * porcentagem) + bonusVantagens.
     * O campo bonusVantagens é populado por BONUS_VIDA_MEMBRO antes deste método ser chamado.
     */
    public int calcularVidaMembro(FichaVidaMembro membro, int vidaTotal, BigDecimal porcentagem) {
        if (porcentagem == null) {
            membro.setVida(0);
            return 0;
        }
        BigDecimal resultado = BigDecimal.valueOf(vidaTotal).multiply(porcentagem)
                .setScale(0, RoundingMode.FLOOR);
        int vidaProporcional = resultado.intValue();
        int bonusVantagens = membro.getBonusVantagens() != null ? membro.getBonusVantagens() : 0;
        int vida = vidaProporcional + bonusVantagens;
        membro.setVida(vida);
        return vida;
    }

    /**
     * Calcula a essência total da ficha.
     * Formula: floor((vigorTotal + sabedoriaTotal) / 2) + ficha.nivel + ficha.renascimentos + essencia.vantagens + essencia.outros
     */
    public int calcularEssenciaTotal(Ficha ficha, FichaEssencia essencia, int vigorTotal, int sabedoriaTotal) {
        int nivelFicha = ficha.getNivel() != null ? ficha.getNivel() : 1;
        int renascimentos = ficha.getRenascimentos() != null ? ficha.getRenascimentos() : 0;
        int vantagens = essencia.getVantagens() != null ? essencia.getVantagens() : 0;
        int outros = essencia.getOutros() != null ? essencia.getOutros() : 0;

        int baseEssencia = (int) Math.floor((vigorTotal + sabedoriaTotal) / 2.0);
        int total = baseEssencia + nivelFicha + renascimentos + vantagens + outros;
        essencia.setTotal(total);

        // Inicializa essenciaAtual com total se ainda estiver zerada (nova ficha)
        if (essencia.getEssenciaAtual() == null || essencia.getEssenciaAtual() == 0) {
            essencia.setEssenciaAtual(total);
        }

        return total;
    }

    /**
     * Calcula o total de ameaça da ficha.
     * Formula: ficha.nivel + ameaca.itens + ameaca.titulos + ameaca.renascimentos + ameaca.outros
     */
    public int calcularAmeacaTotal(Ficha ficha, FichaAmeaca ameaca) {
        int nivelFicha = ficha.getNivel() != null ? ficha.getNivel() : 1;
        int itens = ameaca.getItens() != null ? ameaca.getItens() : 0;
        int titulos = ameaca.getTitulos() != null ? ameaca.getTitulos() : 0;
        int renascimentos = ameaca.getRenascimentos() != null ? ameaca.getRenascimentos() : 0;
        int outros = ameaca.getOutros() != null ? ameaca.getOutros() : 0;

        int total = nivelFicha + itens + titulos + renascimentos + outros;
        ameaca.setTotal(total);
        return total;
    }

    /**
     * Recalcula vida, vida por membro, essência e ameaça em sequência.
     */
    public void recalcularEstado(
            Ficha ficha,
            FichaVida vida,
            List<FichaVidaMembro> membros,
            FichaEssencia essencia,
            FichaAmeaca ameaca,
            int vigorTotal,
            int sabedoriaTotal) {

        // 1. Vida total
        int vidaTotal = calcularVidaTotal(ficha, vida, vigorTotal);

        // 2. Vida por membro
        for (FichaVidaMembro membro : membros) {
            BigDecimal porcentagem = membro.getMembroCorpoConfig() != null
                    ? membro.getMembroCorpoConfig().getPorcentagemVida()
                    : null;
            calcularVidaMembro(membro, vidaTotal, porcentagem);
        }

        // 3. Essência
        calcularEssenciaTotal(ficha, essencia, vigorTotal, sabedoriaTotal);

        // 4. Ameaça
        calcularAmeacaTotal(ficha, ameaca);
    }

    // ==================== RECALCULAR TUDO ====================

    /**
     * Recalcula todos os valores derivados da ficha.
     *
     * <p>Sequência completa:</p>
     * <ol>
     *   <li>Reset de campos deriváveis (idempotência)</li>
     *   <li>Bônus raciais (GAP-CALC-03)</li>
     *   <li>Bônus de classe em bônus derivados (GAP-CALC-01)</li>
     *   <li>Bônus de classe em aptidões (GAP-CALC-02)</li>
     *   <li>Atributos → bônus → vida/essência/ameaça</li>
     *   <li>DADO_UP nas prospecções</li>
     * </ol>
     *
     * @param ficha              ficha com raca e classe carregados (para logging)
     * @param atributos          lista de FichaAtributo com AtributoConfig carregado
     * @param aptidoes           lista de FichaAptidao com AptidaoConfig carregado
     * @param bonus              lista de FichaBonus com BonusConfig carregado
     * @param vida               FichaVida da ficha
     * @param membros            lista de FichaVidaMembro com MembroCorpoConfig carregado
     * @param essencia           FichaEssencia da ficha
     * @param ameaca             FichaAmeaca da ficha
     * @param racaBonusAtributos lista de RacaBonusAtributo da raça (carregados via JOIN FETCH)
     * @param classeBonus        lista de ClasseBonus da classe (carregados previamente via JOIN FETCH)
     * @param classeAptidaoBonus lista de ClasseAptidaoBonus da classe (carregados previamente)
     * @param vantagens          lista de FichaVantagem com VantagemConfig.efeitos carregados via JOIN FETCH
     * @param dadosOrdenados     lista de DadoProspeccaoConfig do jogo ordenada por ordemExibicao ASC
     * @param prospeccoes        lista de FichaProspeccao da ficha (para aplicar DADO_UP)
     */
    public void recalcular(
            Ficha ficha,
            List<FichaAtributo> atributos,
            List<FichaAptidao> aptidoes,
            List<FichaBonus> bonus,
            FichaVida vida,
            List<FichaVidaMembro> membros,
            FichaEssencia essencia,
            FichaAmeaca ameaca,
            List<RacaBonusAtributo> racaBonusAtributos,
            List<ClasseBonus> classeBonus,
            List<ClasseAptidaoBonus> classeAptidaoBonus,
            List<FichaVantagem> vantagens,
            List<DadoProspeccaoConfig> dadosOrdenados,
            List<FichaProspeccao> prospeccoes) {

        // PASSO 0: aplicar efeitos de vantagens (antes dos demais cálculos)
        // zerarContribuicoesVantagens é sempre chamado (dentro de aplicarEfeitosVantagens)
        // para garantir idempotência mesmo quando não há vantagens.
        aplicarEfeitosVantagens(
                vantagens != null ? vantagens : List.of(),
                atributos, aptidoes, bonus, vida, membros, essencia,
                dadosOrdenados != null ? dadosOrdenados : List.of(),
                prospeccoes != null ? prospeccoes : List.of());

        // PASSO 1: zerar campos de raça/classe que serão recalculados nos passos 2–4
        resetarCamposDerivaveis(bonus);

        // PASSO 2: aplicar bônus raciais em FichaAtributo.outros (GAP-CALC-03)
        aplicarRacaBonusAtributo(ficha, atributos, racaBonusAtributos);

        // PASSO 3: aplicar bônus de classe em FichaBonus.classe (GAP-CALC-01)
        aplicarClasseBonus(ficha, bonus, classeBonus);

        // PASSO 4: aplicar bônus de classe em FichaAptidao.classe (GAP-CALC-02)
        aplicarClasseAptidaoBonus(ficha, aptidoes, classeAptidaoBonus);

        // PASSO 5: recalcular totais de atributos (total = base + nivel + outros)
        recalcularAtributos(atributos);

        // PASSO 6: recalcular totais de aptidões (total = base + sorte + classe + outros)
        aptidoes.forEach(FichaAptidao::recalcularTotal);

        // PASSO 7: recalcular bônus derivados (base via fórmula + demais parcelas)
        recalcularBonus(atributos, bonus);

        // PASSO 8: encontrar VIG e SAB pelo atributoConfig.abreviacao
        int vigorTotal = atributos.stream()
                .filter(a -> a.getAtributoConfig() != null
                        && "VIG".equalsIgnoreCase(a.getAtributoConfig().getAbreviacao()))
                .mapToInt(a -> a.getTotal() != null ? a.getTotal() : 0)
                .findFirst()
                .orElse(0);

        int sabedoriaTotal = atributos.stream()
                .filter(a -> a.getAtributoConfig() != null
                        && "SAB".equalsIgnoreCase(a.getAtributoConfig().getAbreviacao()))
                .mapToInt(a -> a.getTotal() != null ? a.getTotal() : 0)
                .findFirst()
                .orElse(0);

        // PASSO 9: recalcular estado (vida, membros, essência, ameaça)
        recalcularEstado(ficha, vida, membros, essencia, ameaca, vigorTotal, sabedoriaTotal);
    }

    /**
     * Sobrecarga retrocompatível sem aptidoes, racaBonusAtributos, classeBonus, classeAptidaoBonus, vantagens,
     * dadosOrdenados e prospeccoes. Mantida para não quebrar chamadas existentes.
     *
     * @deprecated Prefira a sobrecarga completa para garantir cálculos corretos.
     */
    @Deprecated(since = "Spec-007-T0", forRemoval = true)
    public void recalcular(
            Ficha ficha,
            List<FichaAtributo> atributos,
            List<FichaBonus> bonus,
            FichaVida vida,
            List<FichaVidaMembro> membros,
            FichaEssencia essencia,
            FichaAmeaca ameaca) {

        recalcular(ficha, atributos, List.of(), bonus, vida, membros, essencia, ameaca,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    // ==================== PROSPECÇÃO ====================

    /**
     * Calcula o dado disponível resultante de todas as vantagens DADO_UP ativas.
     * Retorna o DadoProspeccaoConfig correspondente à maior posição alcançada,
     * ou null se não há vantagem DADO_UP ativa.
     *
     * <p>Lógica: cada nível de DADO_UP avança uma posição na sequência (0-indexed).
     * Múltiplas vantagens DADO_UP: vence a maior posição (MAX, não acumulam).
     * Cap no último dado se o nível excede o tamanho da sequência.</p>
     *
     * @param vantagens      vantagens da ficha com efeitos carregados
     * @param dadosOrdenados lista de DadoProspeccaoConfig ordenada por ordemExibicao ASC
     */
    public DadoProspeccaoConfig calcularDadoUp(
            List<FichaVantagem> vantagens,
            List<DadoProspeccaoConfig> dadosOrdenados) {

        if (dadosOrdenados == null || dadosOrdenados.isEmpty()) return null;

        int posicaoMaxima = -1;

        for (FichaVantagem fichaVantagem : vantagens) {
            if (fichaVantagem.getVantagemConfig() == null) continue;
            int nivel = fichaVantagem.getNivelAtual() != null ? fichaVantagem.getNivelAtual() : 1;

            for (VantagemEfeito efeito : fichaVantagem.getVantagemConfig().getEfeitos()) {
                if (efeito.getDeletedAt() != null) continue;
                if (efeito.getTipoEfeito() != TipoEfeito.DADO_UP) continue;

                int posicaoCandidata = nivel - 1; // 0-indexed: nível 1 → posição 0
                if (posicaoCandidata > posicaoMaxima) {
                    posicaoMaxima = posicaoCandidata;
                }
            }
        }

        if (posicaoMaxima < 0) return null;

        int indiceFinal = Math.min(posicaoMaxima, dadosOrdenados.size() - 1);
        return dadosOrdenados.get(indiceFinal);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Passo 1 da sequência: zera campos de raça/classe que serão recalculados nos passos 2–4.
     * Chamado após zerarContribuicoesVantagens para garantir idempotência completa.
     * NÃO zera campos de entrada manual do Mestre (itens, gloria, outros, etc.).
     *
     * <p>Campos gerenciados pelo PASSO 0 (zerarContribuicoesVantagens):
     * atributo.outros, aptidao.outros, bonus.vantagens, vida.vt,
     * membro.bonusVantagens e essencia.vantagens.</p>
     */
    private void resetarCamposDerivaveis(List<FichaBonus> bonus) {
        bonus.forEach(b -> b.setClasse(0));
        // FichaAptidao.classe NÃO é resetado aqui porque pode ser definido manualmente pelo Mestre.
        // O aplicarClasseAptidaoBonus sobrescreve o valor quando há ClasseAptidaoBonus configurado.
    }

    /**
     * Passo 2 da sequência: aplica bônus raciais em FichaAtributo.outros.
     * Bônus de raça é fixo (não escala com nível) e pode ser negativo.
     * O campo outros já foi zerado no Passo 1 — este método soma em cima de zero.
     *
     * @param ficha              ficha (usada apenas para logging)
     * @param atributos          lista de FichaAtributo da ficha
     * @param racaBonusAtributos lista de RacaBonusAtributo carregados com JOIN FETCH pelo FichaService
     */
    private void aplicarRacaBonusAtributo(
            Ficha ficha,
            List<FichaAtributo> atributos,
            List<RacaBonusAtributo> racaBonusAtributos) {

        if (racaBonusAtributos == null || racaBonusAtributos.isEmpty()) return;

        Map<Long, FichaAtributo> atributosMap = atributos.stream()
                .filter(a -> a.getAtributoConfig() != null)
                .collect(Collectors.toMap(a -> a.getAtributoConfig().getId(), a -> a));

        String nomeRaca = ficha.getRaca() != null ? ficha.getRaca().getNome() : "desconhecida";
        for (RacaBonusAtributo racaBonus : racaBonusAtributos) {
            if (racaBonus.getAtributo() == null) continue;
            FichaAtributo alvo = atributosMap.get(racaBonus.getAtributo().getId());
            if (alvo != null) {
                alvo.setOutros(alvo.getOutros() + racaBonus.getBonus());
            } else {
                log.warn("RacaBonusAtributo: FichaAtributo não encontrado para AtributoConfig ID {} (raça: {})",
                        racaBonus.getAtributo().getId(), nomeRaca);
            }
        }
    }

    /**
     * Passo 3 da sequência: aplica bônus de classe em FichaBonus.classe.
     * Fórmula: round(classeBonus.valorPorNivel * ficha.nivel) para cada BonusConfig da classe.
     * O campo classe já foi zerado no Passo 1 — este método sobrescreve com o valor correto.
     * ClasseBonus é opcional: classes sem bônus configurados retornam lista vazia.
     *
     * @param ficha       ficha com getClasse() carregado
     * @param bonus       lista de FichaBonus da ficha
     * @param classeBonus lista de ClasseBonus da classe (carregados pelo FichaService via JOIN FETCH)
     */
    private void aplicarClasseBonus(Ficha ficha, List<FichaBonus> bonus, List<ClasseBonus> classeBonus) {
        if (ficha.getClasse() == null || classeBonus.isEmpty()) return;

        int nivel = ficha.getNivel() != null ? ficha.getNivel() : 1;

        Map<Long, FichaBonus> bonusMap = bonus.stream()
                .filter(b -> b.getBonusConfig() != null)
                .collect(Collectors.toMap(b -> b.getBonusConfig().getId(), b -> b));

        for (ClasseBonus cb : classeBonus) {
            if (cb.getBonus() == null) continue;
            FichaBonus alvo = bonusMap.get(cb.getBonus().getId());
            if (alvo != null) {
                int valorClasse = cb.getValorPorNivel() != null
                        ? cb.getValorPorNivel().multiply(BigDecimal.valueOf(nivel))
                                .setScale(0, RoundingMode.HALF_UP).intValue()
                        : 0;
                alvo.setClasse(alvo.getClasse() + valorClasse);
            } else {
                log.warn("ClasseBonus: FichaBonus não encontrado para BonusConfig ID {} (classe: {})",
                        cb.getBonus().getId(), ficha.getClasse().getNome());
            }
        }
    }

    /**
     * Passo 4 da sequência: aplica bônus fixo de classe em FichaAptidao.classe.
     * ClasseAptidaoBonus.bonus é FIXO — não multiplica pelo nível.
     * O campo classe já foi zerado no Passo 1 — este método soma sobre zero.
     * ClasseAptidaoBonus é opcional: classes sem bônus de aptidão retornam lista vazia.
     *
     * @param ficha              ficha com getClasse() carregado
     * @param aptidoes           lista de FichaAptidao da ficha
     * @param classeAptidaoBonus lista de ClasseAptidaoBonus (carregados pelo FichaService)
     */
    private void aplicarClasseAptidaoBonus(
            Ficha ficha,
            List<FichaAptidao> aptidoes,
            List<ClasseAptidaoBonus> classeAptidaoBonus) {

        if (ficha.getClasse() == null || classeAptidaoBonus.isEmpty()) return;

        Map<Long, FichaAptidao> aptidoesMap = aptidoes.stream()
                .filter(a -> a.getAptidaoConfig() != null)
                .collect(Collectors.toMap(a -> a.getAptidaoConfig().getId(), a -> a));

        for (ClasseAptidaoBonus cab : classeAptidaoBonus) {
            if (cab.getAptidao() == null) continue;
            FichaAptidao alvo = aptidoesMap.get(cab.getAptidao().getId());
            if (alvo != null) {
                // Sobrescreve (não soma) para garantir idempotência do campo calculado.
                // Aptidões sem ClasseAptidaoBonus mantêm o valor definido manualmente pelo Mestre.
                alvo.setClasse(cab.getBonus() != null ? cab.getBonus() : 0);
            } else {
                log.warn("ClasseAptidaoBonus: FichaAptidao não encontrada para AptidaoConfig ID {} (classe: {})",
                        cab.getAptidao().getId(), ficha.getClasse().getNome());
            }
        }
    }

    /**
     * Aplica efeitos de VantagemConfig sobre os sub-registros da ficha.
     *
     * <p>Processa os tipos de {@link br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito}:
     * BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_VIDA, BONUS_ESSENCIA, BONUS_DERIVADO,
     * BONUS_VIDA_MEMBRO e DADO_UP.
     * Tipo restante (FORMULA_CUSTOMIZADA) é ignorado e será implementado em task subsequente.</p>
     *
     * <p>Chamado no PASSO 0 de recalcular(), antes do reset das demais parcelas e antes
     * de recalcularAtributos(). A ordem é crítica: os bônus de vantagem em atributos devem
     * estar populados antes do cálculo dos totais.</p>
     *
     * @param vantagens      lista de FichaVantagem com VantagemConfig.efeitos carregados via JOIN FETCH
     * @param atributos      lista de FichaAtributo da ficha
     * @param aptidoes       lista de FichaAptidao da ficha
     * @param bonus          lista de FichaBonus da ficha
     * @param vida           FichaVida da ficha
     * @param membros        lista de FichaVidaMembro da ficha
     * @param essencia       FichaEssencia da ficha
     * @param dadosOrdenados lista de DadoProspeccaoConfig ordenada por ordemExibicao ASC
     * @param prospeccoes    lista de FichaProspeccao da ficha
     */
    private void aplicarEfeitosVantagens(
            List<FichaVantagem> vantagens,
            List<FichaAtributo> atributos,
            List<FichaAptidao> aptidoes,
            List<FichaBonus> bonus,
            FichaVida vida,
            List<FichaVidaMembro> membros,
            FichaEssencia essencia,
            List<DadoProspeccaoConfig> dadosOrdenados,
            List<FichaProspeccao> prospeccoes) {

        zerarContribuicoesVantagens(atributos, aptidoes, bonus, vida, membros, essencia);

        Map<Long, FichaAtributo> atributosMap = atributos.stream()
                .filter(a -> a.getAtributoConfig() != null)
                .collect(Collectors.toMap(a -> a.getAtributoConfig().getId(), a -> a));

        Map<Long, FichaAptidao> aptidoesMap = aptidoes.stream()
                .filter(a -> a.getAptidaoConfig() != null)
                .collect(Collectors.toMap(a -> a.getAptidaoConfig().getId(), a -> a));

        Map<Long, FichaBonus> bonusMap = bonus.stream()
                .filter(b -> b.getBonusConfig() != null)
                .collect(Collectors.toMap(b -> b.getBonusConfig().getId(), b -> b));

        Map<Long, FichaVidaMembro> membrosMap = membros.stream()
                .filter(m -> m.getMembroCorpoConfig() != null)
                .collect(Collectors.toMap(m -> m.getMembroCorpoConfig().getId(), m -> m));

        for (FichaVantagem fichaVantagem : vantagens) {
            if (fichaVantagem.getVantagemConfig() == null) {
                log.warn("FichaVantagem ID {} sem VantagemConfig — ignorado", fichaVantagem.getId());
                continue;
            }
            int nivel = fichaVantagem.getNivelAtual() != null ? fichaVantagem.getNivelAtual() : 1;

            for (VantagemEfeito efeito : fichaVantagem.getVantagemConfig().getEfeitos()) {
                if (efeito.getDeletedAt() != null) continue; // RN-001: soft delete

                switch (efeito.getTipoEfeito()) {
                    case BONUS_ATRIBUTO -> {
                        if (efeito.getAtributoAlvo() == null) {
                            log.warn("BONUS_ATRIBUTO sem atributoAlvo — efeito ID {}", efeito.getId());
                            break;
                        }
                        FichaAtributo alvo = atributosMap.get(efeito.getAtributoAlvo().getId());
                        if (alvo != null) {
                            alvo.setOutros(alvo.getOutros() + calcularValorEfeito(efeito, nivel));
                        } else {
                            log.warn("BONUS_ATRIBUTO: FichaAtributo não encontrado para AtributoConfig ID {}",
                                    efeito.getAtributoAlvo().getId());
                        }
                    }
                    case BONUS_APTIDAO -> {
                        if (efeito.getAptidaoAlvo() == null) {
                            log.warn("BONUS_APTIDAO sem aptidaoAlvo — efeito ID {}", efeito.getId());
                            break;
                        }
                        FichaAptidao alvo = aptidoesMap.get(efeito.getAptidaoAlvo().getId());
                        if (alvo != null) {
                            alvo.setOutros(alvo.getOutros() + calcularValorEfeito(efeito, nivel));
                        } else {
                            log.warn("BONUS_APTIDAO: FichaAptidao não encontrada para AptidaoConfig ID {}",
                                    efeito.getAptidaoAlvo().getId());
                        }
                    }
                    case BONUS_VIDA -> {
                        int bonus2 = calcularValorEfeito(efeito, nivel);
                        vida.setVt(vida.getVt() + bonus2);
                    }
                    case BONUS_ESSENCIA -> {
                        int bonus2 = calcularValorEfeito(efeito, nivel);
                        essencia.setVantagens(essencia.getVantagens() + bonus2);
                    }
                    case BONUS_DERIVADO -> {
                        if (efeito.getBonusAlvo() == null) {
                            log.warn("BONUS_DERIVADO sem bonusAlvo — efeito ID {}", efeito.getId());
                            break;
                        }
                        FichaBonus alvoBonus = bonusMap.get(efeito.getBonusAlvo().getId());
                        if (alvoBonus != null) {
                            alvoBonus.setVantagens(alvoBonus.getVantagens() + calcularValorEfeito(efeito, nivel));
                        } else {
                            log.warn("BONUS_DERIVADO: FichaBonus não encontrada para BonusConfig ID {}",
                                    efeito.getBonusAlvo().getId());
                        }
                    }
                    case BONUS_VIDA_MEMBRO -> {
                        if (efeito.getMembroAlvo() == null) {
                            log.warn("BONUS_VIDA_MEMBRO sem membroAlvo — efeito ID {}", efeito.getId());
                            break;
                        }
                        FichaVidaMembro alvoMembro = membrosMap.get(efeito.getMembroAlvo().getId());
                        if (alvoMembro != null) {
                            alvoMembro.setBonusVantagens(alvoMembro.getBonusVantagens() + calcularValorEfeito(efeito, nivel));
                        } else {
                            log.warn("BONUS_VIDA_MEMBRO: FichaVidaMembro não encontrada para MembroCorpoConfig ID {}",
                                    efeito.getMembroAlvo().getId());
                        }
                    }
                    case DADO_UP -> { /* tratado em lote após o loop — ver abaixo */ }
                    default -> { /* FORMULA_CUSTOMIZADA — implementado em task subsequente */ }
                }
            }
        }

        // Aplicar DADO_UP em lote: calcula o dado resultante uma única vez e atualiza todas as prospecções
        DadoProspeccaoConfig dadoResultante = calcularDadoUp(vantagens, dadosOrdenados);
        for (FichaProspeccao prospeccao : prospeccoes) {
            prospeccao.setDadoDisponivel(dadoResultante);
        }
    }

    /**
     * Zera todos os campos que serão recalculados a partir de efeitos de vantagem.
     * Chamado no início de aplicarEfeitosVantagens() para garantir idempotência (RN-007).
     * NÃO zera campos de entrada manual do Mestre.
     */
    private void zerarContribuicoesVantagens(
            List<FichaAtributo> atributos,
            List<FichaAptidao> aptidoes,
            List<FichaBonus> bonus,
            FichaVida vida,
            List<FichaVidaMembro> membros,
            FichaEssencia essencia) {

        atributos.forEach(a -> a.setOutros(0));
        aptidoes.forEach(a -> a.setOutros(0));
        bonus.forEach(b -> b.setVantagens(0));
        vida.setVt(0);
        membros.forEach(m -> m.setBonusVantagens(0));
        essencia.setVantagens(0);
    }

    /**
     * Calcula o valor numérico de um efeito para um dado nível de vantagem.
     * Fórmula: (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem
     */
    private int calcularValorEfeito(VantagemEfeito efeito, int nivelVantagem) {
        double valorFixo = efeito.getValorFixo() != null ? efeito.getValorFixo().doubleValue() : 0.0;
        double valorPorNivel = efeito.getValorPorNivel() != null ? efeito.getValorPorNivel().doubleValue() : 0.0;
        return (int) Math.round(valorFixo + valorPorNivel * nivelVantagem);
    }
}
