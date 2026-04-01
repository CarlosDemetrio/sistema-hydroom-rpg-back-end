package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsável pelo cálculo de valores derivados da Ficha.
 *
 * <p>Recalcula: totais de atributos, ímpeto, base de bônus, vida, vida por membro,
 * essência e ameaça. Não persiste nada - apenas atualiza os campos das entidades
 * recebidas.</p>
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
     * Calcula a vida de um membro do corpo: floor(vidaTotal * porcentagem).
     */
    public int calcularVidaMembro(FichaVidaMembro membro, int vidaTotal, BigDecimal porcentagem) {
        if (porcentagem == null) {
            membro.setVida(0);
            return 0;
        }
        BigDecimal resultado = BigDecimal.valueOf(vidaTotal).multiply(porcentagem)
                .setScale(0, RoundingMode.FLOOR);
        int vida = resultado.intValue();
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
     * <p>Ordem: atributos → bônus → vida/essência/ameaça</p>
     */
    public void recalcular(
            Ficha ficha,
            List<FichaAtributo> atributos,
            List<FichaBonus> bonus,
            FichaVida vida,
            List<FichaVidaMembro> membros,
            FichaEssencia essencia,
            FichaAmeaca ameaca) {

        // 1. Recalcular atributos
        recalcularAtributos(atributos);

        // 2. Recalcular bônus
        recalcularBonus(atributos, bonus);

        // 3. Encontrar VIG e SAB pelo atributoConfig.abreviacao
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

        // 4. Recalcular estado (vida, membros, essência, ameaça)
        recalcularEstado(ficha, vida, membros, essencia, ameaca, vigorTotal, sabedoriaTotal);
    }
}
