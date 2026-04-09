package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ItemConfigDefault;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.ItemEfeitoDefault;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.RaridadeItemConfigDefault;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.TipoItemConfigDefault;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DefaultItensProvider {

    public List<RaridadeItemConfigDefault> getRaridades() {
        return List.of(
            new RaridadeItemConfigDefault("Comum",      "#9d9d9d", 1, true,  0, 0, 0, 0, "Itens mundanos sem encantamento"),
            new RaridadeItemConfigDefault("Incomum",    "#1eff00", 2, false, 1, 1, 1, 1, "Levemente encantado ou de qualidade excepcional"),
            new RaridadeItemConfigDefault("Raro",       "#0070dd", 3, false, 1, 2, 1, 2, "Encantamento moderado, raramente encontrado"),
            new RaridadeItemConfigDefault("Muito Raro", "#a335ee", 4, false, 2, 3, 2, 3, "Encantamento poderoso, obra de artesao mestre"),
            new RaridadeItemConfigDefault("Epico",      "#ff8000", 5, false, 3, 4, 3, 4, "Artefato de grande poder, historia propria"),
            new RaridadeItemConfigDefault("Lendario",   "#e6cc80", 6, false, 4, 5, 4, 5, "Um dos poucos existentes no mundo"),
            new RaridadeItemConfigDefault("Unico",      "#e268a8", 7, false, 0, 0, 0, 0, "Criacao unica do Mestre, sem referencia de custo")
        );
    }

    public List<TipoItemConfigDefault> getTipos() {
        return List.of(
            new TipoItemConfigDefault("Espada Curta",            CategoriaItem.ARMA,       SubcategoriaItem.ESPADA,          false, 1),
            new TipoItemConfigDefault("Espada Longa",            CategoriaItem.ARMA,       SubcategoriaItem.ESPADA,          false, 2),
            new TipoItemConfigDefault("Espada Dupla",            CategoriaItem.ARMA,       SubcategoriaItem.ESPADA,          true,  3),
            new TipoItemConfigDefault("Arco Curto",              CategoriaItem.ARMA,       SubcategoriaItem.ARCO,            true,  4),
            new TipoItemConfigDefault("Arco Longo",              CategoriaItem.ARMA,       SubcategoriaItem.ARCO,            true,  5),
            new TipoItemConfigDefault("Adaga",                   CategoriaItem.ARMA,       SubcategoriaItem.ADAGA,           false, 6),
            new TipoItemConfigDefault("Machado de Batalha",      CategoriaItem.ARMA,       SubcategoriaItem.MACHADO,         false, 7),
            new TipoItemConfigDefault("Machado Grande",          CategoriaItem.ARMA,       SubcategoriaItem.MACHADO,         true,  8),
            new TipoItemConfigDefault("Martelo de Guerra",       CategoriaItem.ARMA,       SubcategoriaItem.MARTELO,         false, 9),
            new TipoItemConfigDefault("Cajado",                  CategoriaItem.ARMA,       SubcategoriaItem.CAJADO,          true,  10),
            new TipoItemConfigDefault("Lanca",                   CategoriaItem.ARMA,       SubcategoriaItem.LANCA,           false, 11),
            new TipoItemConfigDefault("Armadura Leve",           CategoriaItem.ARMADURA,   SubcategoriaItem.ARMADURA_LEVE,   false, 12),
            new TipoItemConfigDefault("Armadura Media",          CategoriaItem.ARMADURA,   SubcategoriaItem.ARMADURA_MEDIA,  false, 13),
            new TipoItemConfigDefault("Armadura Pesada",         CategoriaItem.ARMADURA,   SubcategoriaItem.ARMADURA_PESADA, false, 14),
            new TipoItemConfigDefault("Escudo",                  CategoriaItem.ARMADURA,   SubcategoriaItem.ESCUDO,          false, 15),
            new TipoItemConfigDefault("Anel",                    CategoriaItem.ACESSORIO,  SubcategoriaItem.ANEL,            false, 16),
            new TipoItemConfigDefault("Amuleto",                 CategoriaItem.ACESSORIO,  SubcategoriaItem.AMULETO,         false, 17),
            new TipoItemConfigDefault("Pocao",                   CategoriaItem.CONSUMIVEL, SubcategoriaItem.POCAO,           false, 18),
            new TipoItemConfigDefault("Municao",                 CategoriaItem.CONSUMIVEL, SubcategoriaItem.MUNICAO,         false, 19),
            new TipoItemConfigDefault("Equipamento de Aventura", CategoriaItem.AVENTURA,   SubcategoriaItem.OUTROS,          false, 20)
        );
    }

    public List<ItemConfigDefault> getItens() {
        return List.of(
            // === ARMAS (15 itens) ===
            new ItemConfigDefault("Adaga",             "Comum",   "Adaga",             new BigDecimal("0.45"),  2,    null,  1, "finura, arremesso, leve",              1,  List.of()),
            new ItemConfigDefault("Espada Curta",      "Comum",   "Espada Curta",      new BigDecimal("0.90"),  10,   null,  1, "finura, leve",                         2,  List.of()),
            new ItemConfigDefault("Espada Longa",      "Comum",   "Espada Longa",      new BigDecimal("1.36"),  15,   null,  1, "versatil",                             3,  List.of()),
            new ItemConfigDefault("Espada Longa +1",   "Incomum", "Espada Longa",      new BigDecimal("1.36"),  500,  10,    1, "versatil, magica",                     4,  List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "B.B.A", null, 1)
            )),
            new ItemConfigDefault("Espada Longa +2",   "Raro",    "Espada Longa",      new BigDecimal("1.36"),  5000, 15,    5, "versatil, magica",                     5,  List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "B.B.A", null, 2)
            )),
            new ItemConfigDefault("Machadinha",        "Comum",   "Machado de Batalha",new BigDecimal("0.90"),  5,    null,  1, "leve, arremesso",                      6,  List.of()),
            new ItemConfigDefault("Machado de Batalha","Comum",   "Machado de Batalha",new BigDecimal("1.80"),  10,   null,  1, "versatil",                             7,  List.of()),
            new ItemConfigDefault("Machado Grande",    "Comum",   "Machado Grande",    new BigDecimal("3.17"),  30,   null,  3, "pesado, duas maos",                    8,  List.of()),
            new ItemConfigDefault("Martelo de Guerra", "Comum",   "Martelo de Guerra", new BigDecimal("2.27"),  15,   null,  1, "versatil",                             9,  List.of()),
            new ItemConfigDefault("Arco Curto",        "Comum",   "Arco Curto",        new BigDecimal("0.90"),  25,   null,  1, "duas maos, municao",                   10, List.of()),
            new ItemConfigDefault("Arco Longo",        "Comum",   "Arco Longo",        new BigDecimal("1.80"),  50,   null,  2, "duas maos, municao, pesado",           11, List.of()),
            new ItemConfigDefault("Arco Longo +1",     "Incomum", "Arco Longo",        new BigDecimal("1.80"),  500,  10,    4, "duas maos, municao, magico",           12, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "B.B.A", null, 1)
            )),
            new ItemConfigDefault("Cajado de Madeira", "Comum",   "Cajado",            new BigDecimal("1.80"),  5,    null,  1, "versatil, duas maos",                  13, List.of()),
            new ItemConfigDefault("Cajado Arcano +1",  "Incomum", "Cajado",            new BigDecimal("2.00"),  500,  10,    3, "magico, foco arcano",                  14, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "B.B.M", null, 1)
            )),
            new ItemConfigDefault("Lanca",             "Comum",   "Lanca",             new BigDecimal("1.36"),  1,    null,  1, "arremesso, versatil",                  15, List.of()),

            // === ARMADURAS E ESCUDOS (10 itens) ===
            new ItemConfigDefault("Gibao de Couro",        "Comum",   "Armadura Leve",   new BigDecimal("4.50"),  10,   null,  1, "armadura leve",                              16, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa", null, 1)
            )),
            new ItemConfigDefault("Couro Batido",          "Comum",   "Armadura Leve",   new BigDecimal("11.30"), 45,   null,  1, "armadura leve",                              17, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa", null, 2)
            )),
            new ItemConfigDefault("Camisao de Malha",      "Comum",   "Armadura Media",  new BigDecimal("13.60"), 50,   null,  2, "armadura media",                             18, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa", null, 3)
            )),
            new ItemConfigDefault("Cota de Escamas",       "Comum",   "Armadura Media",  new BigDecimal("20.40"), 50,   null,  3, "armadura media, desvantagem Furtividade",    19, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa", null, 4)
            )),
            new ItemConfigDefault("Cota de Malha",         "Comum",   "Armadura Pesada", new BigDecimal("27.20"), 75,   null,  4, "armadura pesada, Forca minima",              20, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa", null, 5)
            )),
            new ItemConfigDefault("Meia Placa",            "Comum",   "Armadura Pesada", new BigDecimal("19.90"), 750,  null,  5, "armadura pesada",                            21, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa",  null, 5),
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Reflexo", null, 1)
            )),
            new ItemConfigDefault("Placa Completa",        "Raro",    "Armadura Pesada", new BigDecimal("29.50"), 1500, 15,   7, "armadura pesada, magica",                    22, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa", null, 6)
            )),
            new ItemConfigDefault("Escudo de Madeira",     "Comum",   "Escudo",          new BigDecimal("2.72"),  10,   null,  1, "escudo",                                     23, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Bloqueio", null, 1)
            )),
            new ItemConfigDefault("Escudo de Aco",         "Comum",   "Escudo",          new BigDecimal("2.72"),  20,   null,  1, "escudo",                                     24, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Bloqueio", null, 2)
            )),
            new ItemConfigDefault("Escudo Enfeiticado +1", "Incomum", "Escudo",          new BigDecimal("2.72"),  500,  10,   3, "escudo, magico",                             25, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Bloqueio", null, 2),
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa",   null, 1)
            )),

            // === ACESSORIOS E ITENS MAGICOS (5 itens) ===
            new ItemConfigDefault("Anel da Forca +1",    "Raro",      "Anel",    new BigDecimal("0.01"),  2000, null, 5, "magico, unico", 26, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_ATRIBUTO, null, "FOR", 1)
            )),
            new ItemConfigDefault("Anel de Protecao +1", "Raro",      "Anel",    new BigDecimal("0.01"),  2000, null, 5, "magico",        27, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Defesa",   null, 1),
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Bloqueio", null, 1)
            )),
            new ItemConfigDefault("Amuleto de Saude",    "Incomum",   "Amuleto", new BigDecimal("0.05"),  500,  null, 3, "magico",        28, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_VIDA, null, null, 5)
            )),
            new ItemConfigDefault("Amuleto da Essencia", "Incomum",   "Amuleto", new BigDecimal("0.05"),  500,  null, 3, "magico",        29, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_ESSENCIA, null, null, 5)
            )),
            new ItemConfigDefault("Manto de Elvenkind",  "Muito Raro","Amuleto", new BigDecimal("0.45"),  5000, null, 7, "magico",        30, List.of(
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Esquiva",   null, 3),
                new ItemEfeitoDefault(TipoItemEfeito.BONUS_DERIVADO, "Percepcao", null, 2)
            )),

            // === CONSUMIVEIS (5 itens) ===
            new ItemConfigDefault("Pocao de Cura Menor",    "Comum",   "Pocao",   new BigDecimal("0.45"), 25,  1, 1, "consumivel, recupera 5 de vida",  31, List.of()),
            new ItemConfigDefault("Pocao de Cura",          "Comum",   "Pocao",   new BigDecimal("0.45"), 50,  1, 1, "consumivel, recupera 10 de vida", 32, List.of()),
            new ItemConfigDefault("Pocao de Cura Superior", "Incomum", "Pocao",   new BigDecimal("0.45"), 200, 1, 3, "consumivel, recupera 25 de vida", 33, List.of()),
            new ItemConfigDefault("Flecha Comum (20)",      "Comum",   "Municao", new BigDecimal("0.45"), 1,   null, 1, "municao para arcos",            34, List.of()),
            new ItemConfigDefault("Virote (20)",            "Comum",   "Municao", new BigDecimal("0.36"), 1,   null, 1, "municao para bestas",           35, List.of()),

            // === EQUIPAMENTOS DE AVENTURA (5 itens) ===
            new ItemConfigDefault("Kit de Aventureiro",  "Comum", "Equipamento de Aventura", new BigDecimal("12.00"), 12, null, 1, "mochila, racao 10 dias, corda, archote",                36, List.of()),
            new ItemConfigDefault("Kit de Curandeiro",   "Comum", "Equipamento de Aventura", new BigDecimal("1.50"),  5,  10,   1, "10 usos de bandagem, 5 usos de antidoto",               37, List.of()),
            new ItemConfigDefault("Kit de Ladroa",       "Comum", "Equipamento de Aventura", new BigDecimal("0.90"),  25, null, 1, "ferramentas de ladroa, forcado VIG para abrir fechaduras",38, List.of()),
            new ItemConfigDefault("Lanterna Bullseye",   "Comum", "Equipamento de Aventura", new BigDecimal("1.00"),  10, null, 1, "iluminacao direcional 18m, 6h de oleo",                 39, List.of()),
            new ItemConfigDefault("Tomo Arcano",         "Comum", "Equipamento de Aventura", new BigDecimal("1.50"),  25, null, 1, "livro de feiticos para Magos e Feiticeiros",             40, List.of())
        );
    }
}
