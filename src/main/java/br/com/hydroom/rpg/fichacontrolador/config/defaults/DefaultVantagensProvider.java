package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.CategoriaVantagemDTO;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.VantagemConfigDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DefaultVantagensProvider {

    public List<CategoriaVantagemDTO> getCategorias() {
        return List.of(
            CategoriaVantagemDTO.of("Treinamento Físico",       "#e74c3c", 1),
            CategoriaVantagemDTO.of("Treinamento Mental",       "#8e44ad", 2),
            CategoriaVantagemDTO.of("Ação",                     "#e67e22", 3),
            CategoriaVantagemDTO.of("Reação",                   "#27ae60", 4),
            CategoriaVantagemDTO.of("Vantagem de Atributo",     "#2980b9", 5),
            CategoriaVantagemDTO.of("Vantagem Geral",           "#95a5a6", 6),
            CategoriaVantagemDTO.of("Vantagem Histórica",       "#f39c12", 7),
            CategoriaVantagemDTO.of("Vantagem de Renascimento", "#1abc9c", 8),
            CategoriaVantagemDTO.of("Vantagem Racial",          "#7f8c8d", 9)
        );
    }

    public List<VantagemConfigDTO> getVantagens() {
        List<VantagemConfigDTO> result = new ArrayList<>();
        result.addAll(buildTreinamentoFisico());
        result.addAll(buildTreinamentoMental());
        result.addAll(buildAcao());
        result.addAll(buildReacao());
        result.addAll(buildAtributo());
        result.addAll(buildGeral());
        result.addAll(buildHistorica());
        result.addAll(buildRenascimento());
        result.addAll(buildRaciais());
        return Collections.unmodifiableList(result);
    }

    // === Helper para reduzir boilerplate ===
    private VantagemConfigDTO vantagem(String sigla, String nome, String descricao,
                                       int nivelMax, String formulaCusto,
                                       String efeito, String tipo,
                                       String categoria, int ordem) {
        return VantagemConfigDTO.builder()
                .sigla(sigla)
                .nome(nome)
                .descricao(descricao)
                .nivelMaximoVantagem(nivelMax)
                .formulaCusto(formulaCusto)
                .valorBonusFormula(efeito)
                .tipoVantagem(tipo)
                .categoriaNome(categoria)
                .ordemExibicao(ordem)
                .build();
    }

    private List<VantagemConfigDTO> buildTreinamentoFisico() {
        final String CAT = "Treinamento Físico";
        return List.of(
            vantagem("VTCO", "Treinamento em Combate Ofensivo",
                "Treinamento especializado em técnicas ofensivas de combate físico. O dado é elevado até D10 onde um novo dado se inicia no D3.",
                10, "4", "+1 B.B.A e 1 dado de dano (D3→D.UP) por nivel", "VANTAGEM", CAT, 1),
            vantagem("VTCD", "Treinamento em Combate Defensivo",
                "Treinamento especializado em técnicas defensivas. Os dados geram RD natural resistente a danos por contusão.",
                10, "4", "+1 Bloqueio e 1 dado de RD natural (D3→D.UP) por nivel", "VANTAGEM", CAT, 2),
            vantagem("VTCE", "Treinamento em Combate Evasivo",
                "Treinamento especializado em evasão. Recebe 2 de bônus de Reflexo por nível ao invés de 1.",
                10, "2", "+2 Reflexo por nivel", "VANTAGEM", CAT, 3)
        );
    }

    private List<VantagemConfigDTO> buildTreinamentoMental() {
        final String CAT = "Treinamento Mental";
        return List.of(
            vantagem("VTM",  "Treinamento Magico",
                "Treinamento especializado em técnicas mágicas. O dado é elevado até D10 onde um novo dado se inicia no D3.",
                10, "4", "+1 B.B.M e 1 dado de dano magico (D3→D.UP) por nivel", "VANTAGEM", CAT, 4),
            vantagem("VTPM", "Treinamento em Percepcao Magica",
                "Treinamento em percepção de auras e manifestações mágicas. Recebe 2 de bônus por nível.",
                10, "2", "+2 Percepcao por nivel", "VANTAGEM", CAT, 5),
            vantagem("VTL",  "Treinamento Logico",
                "Treinamento especializado em raciocínio lógico e dedutivo. Requer B.RAC 5+.",
                5,  "4", "+1 B.B.M por nivel", "VANTAGEM", CAT, 6),
            vantagem("VTMA", "Treinamento em Manipulacao",
                "Treinamento em técnicas mentais de manipulação. Requer B.B.M 8+.",
                3,  "3", "+2 em Aptidoes Mentais por nivel", "VANTAGEM", CAT, 7)
        );
    }

    private List<VantagemConfigDTO> buildAcao() {
        final String CAT = "Ação";
        return List.of(
            vantagem("VAA", "Ataque Adicional",
                "Permite realizar um ataque extra após a ação ofensiva principal. Requer Bônus Ofensivo 15+.",
                1, "10", "Um ataque adicional apos a acao ofensiva", "VANTAGEM", CAT, 8),
            vantagem("VAS", "Ataque Sentai",
                "Em ataque conjunto força percepção do alvo usando a maior soma dos atacantes. Requer Raciocínio 5+.",
                1, "10", "Ataque conjunto usa maior soma e forca percepcao do alvo", "VANTAGEM", CAT, 9)
        );
    }

    private List<VantagemConfigDTO> buildReacao() {
        final String CAT = "Reação";
        return List.of(
            vantagem("VCA",  "Contra-Ataque",
                "Pode reagir a um ataque atacando de volta com dificuldade +5. Requer Bônus Base 10+.",
                1, "5", "Reacao: atacar de volta com dificuldade +5", "VANTAGEM", CAT, 10),
            vantagem("VITC", "Interceptacao",
                "Interrompe uma ação antes dela de fato acontecer. Requer Bônus Base 10+.",
                1, "5", "Reacao: interrompe acao do oponente antes de ocorrer", "VANTAGEM", CAT, 11),
            vantagem("VRE",  "Reflexos Especiais",
                "Reações padrão podem ser executadas utilizando habilidades especiais. Requer B.REF ou P.MAG 30+.",
                3, "5", "Reacoes padrao executadas com habilidades por nivel", "VANTAGEM", CAT, 12),
            vantagem("VIH",  "Instinto Heroico",
                "Usar a ação padrão para salvar um aliado em perigo iminente. Requer Bônus Ofensivo 18+.",
                1, "5", "Acao padrao usada para salvar um aliado", "VANTAGEM", CAT, 13),
            vantagem("VDH",  "Deflexao Heroica",
                "Usar a ação padrão para salvar a si mesmo e outro com dificuldade +5. Requer Bônus Ofensivo 18+.",
                1, "5", "Acao padrao para salvar a si e outro com dificuldade +5", "VANTAGEM", CAT, 14),
            vantagem("VISB", "Instinto de Sobrevivencia",
                "Reduz somas de dificuldade para desviar ataques de múltiplos alvos. Requer Base Reflexos 7+.",
                3, "3", "-1 por nivel na dificuldade ao desviar de ataques multiplos", "VANTAGEM", CAT, 15),
            vantagem("VRA",  "Reflexos Aprimorados",
                "Reduz somas de dificuldade para reduzir dano pela metade. Requer Base Reflexos 7+.",
                3, "3", "-1 por nivel na dificuldade ao reduzir dano pela metade", "VANTAGEM", CAT, 16)
        );
    }

    private List<VantagemConfigDTO> buildAtributo() {
        final String CAT = "Vantagem de Atributo";
        return List.of(
            vantagem("VCFM", "Capacidade de Forca Maxima",
                "Desbloqueia uso da Força máxima em danos por contusão. Desbloqueada a cada 10 em Força.",
                1, "6", "Concede 1D3 de dano por contusao com Forca maxima", "VANTAGEM", CAT, 17),
            vantagem("VDM",  "Dominio de Forca",
                "Eleva o dado de dano por contusão concedido pela Capacidade de Força Máxima. Requer CFM.",
                6, "2", "Eleva dado de dano por contusao 1x por nivel (D3→D4→...→D9)", "VANTAGEM", CAT, 18),
            vantagem("VTEN", "Tenacidade",
                "Desbloqueia uso do Vigor máximo em RD por contusão. Desbloqueada a cada 10 em Vigor.",
                1, "6", "Concede 1D3 de RD por contusao com Vigor maximo", "VANTAGEM", CAT, 19),
            vantagem("VDV",  "Dominio de Vigor",
                "Eleva o dado de RD por contusão concedido pela Tenacidade. Requer Tenacidade.",
                2, "2", "Eleva dado de RD por contusao 1x por nivel (D3→D4→D5)", "VANTAGEM", CAT, 20),
            vantagem("VDF",  "Destreza Felina",
                "Reduz penalidades em locais e terrenos difíceis. Desbloqueada a cada 10 em Agilidade.",
                1, "5", "-1 em penalidade de local ou terreno dificil", "VANTAGEM", CAT, 21),
            vantagem("VSG",  "Sabedoria de Gamaiel",
                "Aumenta um aspecto mágico por nível. Aspectos: Dano, Defesa, Bônus, Duração ou Área. Desbloqueada a cada 10 em SAB.",
                3, "3", "+1 nivel em aspecto magico por nivel de vantagem", "VANTAGEM", CAT, 22),
            vantagem("VSAG", "Sentidos Agucados",
                "Aguça um sentido específico concedendo bônus de percepção. Requer Sabedoria 3+.",
                5, "3", "+2 de Percepcao em 1 sentido especifico por nivel", "VANTAGEM", CAT, 23),
            vantagem("VIN",  "Inteligencia de Nyck",
                "Aumenta o multiplicador de raciocínio em 0.5x por nível. Requer Base de RAC 7+.",
                3, "2", "+0.5x no multiplicador de Raciocinio por nivel", "VANTAGEM", CAT, 24)
        );
    }

    private List<VantagemConfigDTO> buildGeral() {
        final String CAT = "Vantagem Geral";
        return List.of(
            vantagem("VSFE", "Saude de Ferro",
                "Aumenta os pontos de vida do personagem. Requer Vigor 3+.",
                4, "3", "+5 de Vida por nivel", "VANTAGEM", CAT, 25),
            vantagem("VCON", "Concentracao",
                "Aumenta os pontos de animus (essência) do personagem.",
                4, "3", "+5 de Animus por nivel", "VANTAGEM", CAT, 26),
            vantagem("VSRQ", "Saque Rapido",
                "Permite sacar armas sem gastar Ponto de Ação. Requer Agilidade 10+.",
                2, "3", "Saca armas sem custo de P.A por nivel", "VANTAGEM", CAT, 27),
            vantagem("VAMB", "Ambidestria",
                "Remove penalidade de usar mão não dominante. Domínio bilateral de armas e ações.",
                1, "5", "Dominio bilateral sem penalidade para mao nao dominante", "VANTAGEM", CAT, 28),
            vantagem("VMF",  "Memoria Fotografica",
                "Possibilita memória visual plena de tudo que foi visto. Requer Raciocínio 10+.",
                1, "10", "Memoria visual completa e fotografica", "VANTAGEM", CAT, 29)
        );
    }

    private List<VantagemConfigDTO> buildHistorica() {
        final String CAT = "Vantagem Histórica";
        return List.of(
            vantagem("VHER", "Heranca",
                "O personagem herdou bens e recursos de família ou mentor. Rola 1D3 de Riqueza aplicada.",
                1, "5", "Rola 1D3 de Riqueza inicial aplicada", "VANTAGEM", CAT, 30),
            vantagem("VRIQ", "Riqueza",
                "Representa acumulação de bens materiais e riquezas. Cada nível custa mais PN que o anterior.",
                3, "nivel * 5", "+1 Grade de Riqueza por nivel (Nv1=5PN, Nv2=10PN, Nv3=15PN)", "VANTAGEM", CAT, 31),
            vantagem("VIA",  "Indole Aplicada",
                "Permite alterar a índole do personagem em relação a um alvo adicional por nível.",
                5, "2", "Muda indole com 1 alvo adicional por nivel", "VANTAGEM", CAT, 32),
            vantagem("VOFI", "Oficios",
                "O personagem conhece profissões e ofícios variados do mundo.",
                2, "2", "+1 Profissao ou Oficio conhecida por nivel", "VANTAGEM", CAT, 33),
            vantagem("VTOF", "Treino de Oficio",
                "Aprimora a performance do personagem no exercício de ofícios e profissões.",
                10, "4", "+1 por nivel em testes para exercer oficios", "VANTAGEM", CAT, 34),
            vantagem("VVO",  "Vinculo com Organizacao",
                "O personagem possui influência e contatos em uma organização com dezenas de membros.",
                3, "7", "Influencia em organizacao com dezenas de membros por nivel", "VANTAGEM", CAT, 35),
            vantagem("VCAP", "Capangas",
                "O personagem possui aliados ou capangas leais que o seguem.",
                5, "5", "+1 aliado ou capanga leal por nivel", "VANTAGEM", CAT, 36)
        );
    }

    private List<VantagemConfigDTO> buildRenascimento() {
        final String CAT = "Vantagem de Renascimento";
        return List.of(
            vantagem("VCDA", "Controle de Dano",
                "Permite rolar o dado de dano e decidir quanto efetivamente aplicar. Requer 1 Renascimento.",
                1,  "5",  "Decide quanto do dano rolado aplicar ao alvo", "VANTAGEM", CAT, 37),
            vantagem("VUSI", "Ultimo Sigilo",
                "Oculta completamente a manifestação visual e sensorial de habilidades mágicas. Requer 1 Renascimento.",
                1,  "5",  "Oculta manifestacao magica de habilidades", "VANTAGEM", CAT, 38),
            vantagem("VESC", "Escaramuca",
                "Permite realizar ataques falsos e manobras avançadas de combate. Requer 1 Renascimento.",
                3,  "3",  "Ataques falsos e manobras avancadas de combate por nivel", "VANTAGEM", CAT, 39),
            vantagem("VPCO", "Previsao em Combate",
                "Antecipa ações em combate afetando Defesa, Ofensiva ou Reatividade. Requer 2 Renascimentos.",
                3,  "15", "Por nivel: +1 em Defesa, Ofensiva ou Reatividade (escolha)", "VANTAGEM", CAT, 40),
            vantagem("VAI",  "Armas Improvisadas",
                "Permite usar armas improvisadas com eficácia crescente. Requer 1 Renascimento.",
                10, "4",  "+1 B.B.A e 1 dado (D3→D.UP) por nivel com armas improvisadas", "VANTAGEM", CAT, 41),
            vantagem("VDNL", "Dano Nao Letal",
                "Converte todos os danos causados em danos não letais por contusão. Requer 1 Renascimento.",
                1,  "5",  "Converte danos para tipo contusao (nao letal)", "VANTAGEM", CAT, 42),
            vantagem("VAEC", "Acao em Cadeia",
                "Permite agir e atacar durante o Ataque Adicional no mesmo turno. Requer 1 Renascimento e Ataque Adicional.",
                1,  "10", "Agir e atacar durante o Ataque Adicional", "VANTAGEM", CAT, 43),
            vantagem("VATD", "Atencao Difusa",
                "Expande o raio de atenção ao redor do personagem em 1 metro por nível. Requer 1 Renascimento.",
                10, "5",  "+1 metro de raio de atencao ao redor por nivel", "VANTAGEM", CAT, 44),
            vantagem("VSNM", "Senso Numerico",
                "Habilidade de precisão numérica excepcional para cálculos instantâneos. Requer 1 Renascimento.",
                1,  "10", "Precisao numerica instantanea em qualquer calculo", "VANTAGEM", CAT, 45),
            vantagem("VPBF", "Pensamento Bifurcado",
                "Permite executar ações Independente e Padrão simultaneamente no mesmo turno. Requer 1 Renascimento.",
                1,  "10", "Executa acoes Independente e Padrao ao mesmo tempo", "VANTAGEM", CAT, 46),
            vantagem("VMEI", "Memoria Eidetica",
                "Lembra de experiências com detalhes completos de todos os sentidos. Requer 1 Renascimento.",
                1,  "10", "Memoria completa e precisa com todos os sentidos", "VANTAGEM", CAT, 47)
        );
    }

    private List<VantagemConfigDTO> buildRaciais() {
        final String CAT = "Vantagem Racial";
        final String TIPO = "INSOLITUS";
        final String CUSTO = "0";
        return List.of(
            // Karzarcryer
            vantagem("VENF", "Elemento Natural: Fogo",
                "Os Karzarcryer possuem afinidade elemental inata com o fogo. Seu sangue draconiano reage ao fogo como fonte de vida, convertendo exposição ao calor extremo em energia vital.",
                1, CUSTO, "Afinidade com fogo como fonte de vida", TIPO, CAT, 48),
            vantagem("VIEF", "Imunidade Elemental: Fogo",
                "Herança draconiana confere imunidade total a qualquer forma de dano do elemento fogo, sejam chamas naturais, feitiços ígneos ou ambientes vulcânicos.",
                1, CUSTO, "Imunidade total a dano do elemento fogo", TIPO, CAT, 49),
            vantagem("VESD", "Estomago de Dragao",
                "O sistema digestivo dos Karzarcryer pode processar substâncias tóxicas, ígneas ou corrosivas sem qualquer efeito adverso. Podem consumir metal fundido, venenos ou compostos ácidos.",
                1, CUSTO, "Processa substancias toxicas, igneas ou corrosivas sem efeitos", TIPO, CAT, 50),
            // Ikaruz
            vantagem("VASA", "Membro Adicional: Asas",
                "Os Ikarúz possuem asas funcionais que permitem voo sustentado. Apesar do nanismo corporal, suas asas são proporcionalmente grandes e poderosas.",
                1, CUSTO, "Asas funcionais que permitem voo sustentado", TIPO, CAT, 51),
            vantagem("VADA", "Adaptacao Atmosferica",
                "Biologicamente adaptados a qualquer altitude e pressão atmosférica, os Ikarúz não sofrem penalidades por ambiente de altitude extrema, pressão reduzida ou ventos violentos.",
                1, CUSTO, "Sem penalidades por altitude extrema ou pressao reduzida", TIPO, CAT, 52),
            vantagem("VCAL", "Combate Alado",
                "Técnicas de combate que exploram a mobilidade aérea única dos Ikarúz. Utilizam asas como armas auxiliares e para manobras evasivas em combate.",
                3, CUSTO, "Asas usadas como armas e para manobras evasivas em combate", TIPO, CAT, 53),
            // Hankraz
            vantagem("VPIR", "Piercings Raciais",
                "Os Hankráz nascem com piercings de metal rúnico integrados ao corpo. Estes piercings amplificam capacidades sobrenaturais e funcionam como condutores de energia mística.",
                1, CUSTO, "Piercings runicos amplificam capacidades sobrenaturais", TIPO, CAT, 54),
            vantagem("VCEG", "Corpo Esguio",
                "A estrutura corporal extremamente delgada dos Hankráz confere vantagens naturais em furtividade, passagens estreitas e esquiva de ataques.",
                1, CUSTO, "Vantagens em furtividade, passagens estreitas e esquiva", TIPO, CAT, 55),
            vantagem("VVEM", "Vagante entre Mundos",
                "Habilidade única dos Hankráz de perceber e, em alguns casos, acessar brevemente planos de existência paralelos. Podem sentir ecos de outros mundos e criaturas planares.",
                1, CUSTO, "Percebe e acessa brevemente planos de existencia paralelos", TIPO, CAT, 56),
            // Humano
            vantagem("VAHU", "Adaptabilidade Humana",
                "Os humanos são mestres da adaptação. Esta vantagem reflete sua capacidade única de desenvolver proficiência em qualquer área de conhecimento ou habilidade.",
                1, CUSTO, "Proficiencia em qualquer area de conhecimento ou habilidade", TIPO, CAT, 57),
            vantagem("VRHU", "Resiliencia Humana",
                "A força de vontade humana permite recuperação acelerada de condições adversas como veneno, doenças, fadiga e condições debilitantes.",
                1, CUSTO, "Recuperacao acelerada de veneno, doencas, fadiga e debilitacoes", TIPO, CAT, 58),
            vantagem("VVHU", "Versatilidade Humana",
                "Os humanos não possuem restrições raciais no aprendizado. Podem aprender vantagens de qualquer categoria sem pré-requisitos relacionados à raça ou linhagem.",
                1, CUSTO, "Aprende vantagens de qualquer categoria sem restricoes raciais", TIPO, CAT, 59),
            vantagem("VEIN", "Espirito Inabalavel",
                "A mente humana possui resistência natural a influências sobrenaturais. Efeitos de medo, encantamento e manipulação mental têm eficácia reduzida contra humanos.",
                1, CUSTO, "Resistencia natural a medo, encantamento e manipulacao mental", TIPO, CAT, 60),
            vantagem("VLCI", "Legado de Civilizacao",
                "Os humanos construíram as maiores civilizações de Klayrah. Esta herança confere habilidades naturais em negociação, diplomacia e navegação em estruturas sociais.",
                1, CUSTO, "Habilidades em negociacao, diplomacia e estruturas sociais", TIPO, CAT, 61),
            // Anakarys
            vantagem("VANA", "Armas Naturais Aprimoradas",
                "Os Anakarys possuem garras, presas e membros naturais de combate aprimorados. Suas armas naturais são mais afiadas e poderosas que as de outras raças.",
                1, CUSTO, "Garras e presas mais afiadas e poderosas", TIPO, CAT, 62),
            vantagem("VDES", "Deslocamento Especial",
                "Os Anakarys possuem um modo único de movimento — escalar, rastejar ou saltar com eficiência sobrenatural, navegando terrenos intransponíveis para outras raças.",
                1, CUSTO, "Escalar, rastejar e saltar com eficiencia sobrenatural", TIPO, CAT, 63),
            vantagem("VAAR", "Ataque Adicional Racial",
                "Versão racial e limitada do Ataque Adicional. Os Anakarys podem realizar um ataque extra por turno, mas apenas com armas naturais e somente contra alvos de tamanho igual ou menor.",
                1, CUSTO, "Ataque extra por turno apenas com armas naturais vs alvos iguais ou menores", TIPO, CAT, 64)
        );
    }
}
