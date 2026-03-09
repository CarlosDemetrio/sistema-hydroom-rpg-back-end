package br.com.hydroom.rpg.fichacontrolador.constants;

/**
 * Mensagens de validação padronizadas para uso em todo o sistema.
 * Centralizadas para facilitar manutenção e internacionalização futura.
 */
public final class ValidationMessages {

    private ValidationMessages() {
        throw new UnsupportedOperationException("Classe de constantes não pode ser instanciada");
    }

    // ===== MENSAGENS GENÉRICAS =====
    public static final String CAMPO_OBRIGATORIO = "Este campo é obrigatório";
    public static final String CAMPO_INVALIDO = "Campo inválido";

    // ===== USUÁRIO =====
    public static final class Usuario {
        public static final String EMAIL_OBRIGATORIO = "Email é obrigatório";
        public static final String EMAIL_INVALIDO = "Email deve ser válido";
        public static final String EMAIL_TAMANHO = "Email deve ter no máximo 255 caracteres";

        public static final String NOME_OBRIGATORIO = "Nome é obrigatório";
        public static final String NOME_TAMANHO = "Nome deve ter entre 2 e 100 caracteres";

        public static final String IMAGEM_URL_TAMANHO = "URL da imagem deve ter no máximo 500 caracteres";

        public static final String PROVIDER_OBRIGATORIO = "Provider é obrigatório";
        public static final String PROVIDER_TAMANHO = "Provider deve ter no máximo 50 caracteres";

        public static final String PROVIDER_ID_OBRIGATORIO = "Provider ID é obrigatório";
        public static final String PROVIDER_ID_TAMANHO = "Provider ID deve ter no máximo 255 caracteres";

        private Usuario() {}
    }

    // ===== FICHA =====
    public static final class Ficha {
        public static final String JOGO_OBRIGATORIO = "Jogo é obrigatório";
        public static final String USUARIO_OBRIGATORIO = "Usuário é obrigatório";

        public static final String NOME_OBRIGATORIO = "Nome do personagem é obrigatório";
        public static final String NOME_TAMANHO = "Nome deve ter entre 3 e 100 caracteres";

        public static final String JOGADOR_NOME_TAMANHO = "Nome do jogador deve ter no máximo 100 caracteres";
        public static final String TITULO_TAMANHO = "Título heroico deve ter no máximo 200 caracteres";
        public static final String INSOLITUS_TAMANHO = "Insolitus deve ter no máximo 200 caracteres";
        public static final String ORIGEM_TAMANHO = "Origem deve ter no máximo 200 caracteres";
        public static final String GENERO_TAMANHO = "Gênero deve ter no máximo 20 caracteres";
        public static final String ARQUETIPO_TAMANHO = "Arquétipo de referência deve ter no máximo 200 caracteres";

        public static final String IDADE_MINIMA = "Idade mínima é 0";
        public static final String IDADE_MAXIMA = "Idade máxima é 9999";
        public static final String ALTURA_MINIMA = "Altura mínima é 0 cm";
        public static final String ALTURA_MAXIMA = "Altura máxima é 999 cm";
        public static final String PESO_MINIMO = "Peso mínimo é 0 kg";
        public static final String PESO_MAXIMO = "Peso máximo é 999.99 kg";
        public static final String COR_CABELO_TAMANHO = "Cor do cabelo deve ter no máximo 50 caracteres";
        public static final String TAMANHO_CABELO_TAMANHO = "Tamanho do cabelo deve ter no máximo 50 caracteres";
        public static final String COR_OLHOS_TAMANHO = "Cor dos olhos deve ter no máximo 50 caracteres";

        public static final String NIVEL_OBRIGATORIO = "Nível é obrigatório";
        public static final String NIVEL_MINIMO = "Nível mínimo é 1";
        public static final String NIVEL_MAXIMO = "Nível máximo é 99";

        public static final String EXPERIENCIA_OBRIGATORIA = "Experiência é obrigatória";
        public static final String EXPERIENCIA_MINIMA = "Experiência mínima é 0";

        public static final String RENASCIMENTOS_MINIMO = "Renascimentos mínimo é 0";

        private Ficha() {}
    }

    // ===== ATRIBUTOS RPG =====
    public static final class Atributos {
        public static final String FORCA_MINIMO = "Força mínima é 1";
        public static final String FORCA_MAXIMO = "Força máxima é 20";

        public static final String DESTREZA_MINIMO = "Destreza mínima é 1";
        public static final String DESTREZA_MAXIMO = "Destreza máxima é 20";

        public static final String CONSTITUICAO_MINIMO = "Constituição mínima é 1";
        public static final String CONSTITUICAO_MAXIMO = "Constituição máxima é 20";

        public static final String INTELIGENCIA_MINIMO = "Inteligência mínima é 1";
        public static final String INTELIGENCIA_MAXIMO = "Inteligência máxima é 20";

        public static final String SABEDORIA_MINIMO = "Sabedoria mínima é 1";
        public static final String SABEDORIA_MAXIMO = "Sabedoria máxima é 20";

        public static final String CARISMA_MINIMO = "Carisma mínimo é 1";
        public static final String CARISMA_MAXIMO = "Carisma máximo é 20";

        public static final String ATRIBUTO_FORA_RANGE = "%s deve estar entre 1 e 20, valor recebido: %d";

        private Atributos() {}
    }

    // ===== JOGO =====
    public static final class Jogo {
        public static final String NOME_OBRIGATORIO = "Nome do jogo é obrigatório";
        public static final String NOME_TAMANHO = "Nome deve ter entre 3 e 200 caracteres";

        public static final String DESCRICAO_TAMANHO = "Descrição deve ter no máximo 5000 caracteres";

        public static final String MESTRE_OBRIGATORIO = "Mestre é obrigatório";

        public static final String IMAGEM_URL_TAMANHO = "URL da imagem deve ter no máximo 2000 caracteres";

        private Jogo() {}
    }

    // ===== CLASSE PERSONAGEM =====
    public static final class ClassePersonagem {
        public static final String NOME_OBRIGATORIO = "Nome da classe é obrigatório";
        public static final String NOME_TAMANHO = "Nome deve ter entre 2 e 100 caracteres";

        public static final String DESCRICAO_TAMANHO = "Descrição deve ter no máximo 2000 caracteres";

        public static final String JOGO_OBRIGATORIO = "Jogo é obrigatório";

        private ClassePersonagem() {}
    }

    // ===== RAÇA =====
    public static final class Raca {
        public static final String NOME_OBRIGATORIO = "Nome da raça é obrigatório";
        public static final String NOME_TAMANHO = "Nome deve ter entre 2 e 100 caracteres";

        public static final String DESCRICAO_TAMANHO = "Descrição deve ter no máximo 2000 caracteres";

        public static final String JOGO_OBRIGATORIO = "Jogo é obrigatório";

        private Raca() {}
    }

    // ===== FICHA ATRIBUTO =====
    public static final class FichaAtributo {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";
        public static final String ATRIBUTO_CONFIG_OBRIGATORIO = "Configuração de atributo é obrigatória";

        private FichaAtributo() {}
    }

    // ===== FICHA APTIDAO =====
    public static final class FichaAptidao {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";
        public static final String APTIDAO_CONFIG_OBRIGATORIA = "Configuração de aptidão é obrigatória";

        private FichaAptidao() {}
    }

    // ===== FICHA BONUS =====
    public static final class FichaBonus {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";
        public static final String BONUS_CONFIG_OBRIGATORIO = "Configuração de bônus é obrigatória";

        private FichaBonus() {}
    }

    // ===== FICHA VIDA =====
    public static final class FichaVida {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";

        private FichaVida() {}
    }

    // ===== FICHA VIDA MEMBRO =====
    public static final class FichaVidaMembro {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";
        public static final String MEMBRO_CONFIG_OBRIGATORIO = "Configuração de membro do corpo é obrigatória";
        public static final String DANO_MINIMO = "Dano recebido não pode ser negativo";

        private FichaVidaMembro() {}
    }

    // ===== FICHA ESSENCIA =====
    public static final class FichaEssencia {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";
        public static final String GASTO_MINIMO = "Gasto de essência não pode ser negativo";

        private FichaEssencia() {}
    }

    // ===== FICHA AMEACA =====
    public static final class FichaAmeaca {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";
        public static final String ITENS_MINIMO = "Valor de itens não pode ser negativo";
        public static final String TITULOS_MINIMO = "Valor de títulos não pode ser negativo";

        private FichaAmeaca() {}
    }

    // ===== FICHA VANTAGEM =====
    public static final class FichaVantagem {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";
        public static final String VANTAGEM_CONFIG_OBRIGATORIA = "Configuração de vantagem é obrigatória";
        public static final String NIVEL_MINIMO = "Nível da vantagem deve ser no mínimo 1";
        public static final String CUSTO_MINIMO = "Custo pago não pode ser negativo";

        private FichaVantagem() {}
    }

    // ===== FICHA PROSPECCAO =====
    public static final class FichaProspeccao {
        public static final String FICHA_OBRIGATORIA = "Ficha é obrigatória";
        public static final String DADO_CONFIG_OBRIGATORIO = "Configuração de dado de prospecção é obrigatória";
        public static final String QUANTIDADE_MINIMA = "Quantidade não pode ser negativa";

        private FichaProspeccao() {}
    }

    // ===== AUTENTICAÇÃO E SEGURANÇA =====
    public static final class Seguranca {
        public static final String NAO_AUTENTICADO = "Usuário não autenticado";
        public static final String ACESSO_NEGADO = "Acesso negado";
        public static final String SESSAO_EXPIRADA = "Sessão expirada";
        public static final String TOKEN_INVALIDO = "Token inválido";
        public static final String PERMISSAO_INSUFICIENTE = "Você não tem permissão para realizar esta ação";
        public static final String APENAS_MESTRE = "Apenas mestres podem realizar esta ação";

        private Seguranca() {}
    }

    // ===== ATRIBUTO CONFIG =====
    public static final class AtributoConfig {
        public static final String FORMULA_IMPETO_SINTAXE_INVALIDA = "Fórmula de ímpeto com sintaxe inválida";
        public static final String FORMULA_IMPETO_VARIAVEIS_INVALIDAS =
            "Fórmula de ímpeto usa variáveis não permitidas: %s. Permitida: total";

        private AtributoConfig() {}
    }

    // ===== BONUS CONFIG =====
    public static final class BonusConfig {
        public static final String FORMULA_BASE_SINTAXE_INVALIDA = "Fórmula base com sintaxe inválida";
        public static final String FORMULA_BASE_VARIAVEIS_INVALIDAS =
            "Fórmula base usa variáveis não registradas no jogo: %s";

        private BonusConfig() {}
    }

    // ===== VANTAGEM CONFIG =====
    public static final class VantagemConfig {
        public static final String FORMULA_CUSTO_SINTAXE_INVALIDA = "Fórmula de custo com sintaxe inválida";
        public static final String FORMULA_CUSTO_VARIAVEIS_INVALIDAS =
            "Fórmula de custo usa variáveis inválidas: %s. Permitidas: custo_base, nivel_vantagem";

        private VantagemConfig() {}
    }

    // ===== VANTAGEM PRÉ-REQUISITO =====
    public static final class VantagemPreRequisito {
        public static final String AUTO_REFERENCIA =
            "Uma vantagem não pode ser pré-requisito de si mesma.";
        public static final String CICLO_DETECTADO =
            "Adicionar este pré-requisito criaria uma dependência circular entre vantagens.";
        public static final String JA_EXISTE =
            "Este pré-requisito já está registrado para esta vantagem.";
        public static final String JOGOS_DIFERENTES =
            "O pré-requisito deve pertencer ao mesmo jogo da vantagem.";

        private VantagemPreRequisito() {}
    }

    // ===== SIGLAS =====
    public static final class Sigla {
        public static final String SIGLA_JA_EM_USO =
            "Sigla '%s' já está em uso em %s neste jogo. Siglas devem ser únicas por jogo.";

        private Sigla() {}
    }

    // ===== ERROS GERAIS =====
    public static final class Erro {
        public static final String INTERNO = "Erro interno do servidor";
        public static final String NAO_ENCONTRADO = "Recurso não encontrado";
        public static final String JA_EXISTE = "Este recurso já existe";
        public static final String OPERACAO_INVALIDA = "Operação inválida";
        public static final String DADOS_INVALIDOS = "Dados inválidos";
        public static final String CONFLITO = "Conflito ao processar requisição";
        public static final String INTEGRIDADE_DADOS = "Violação de integridade dos dados";

        private Erro() {}
    }

    // ===== VALIDAÇÕES DE FORMATO =====
    public static final class Formato {
        public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String NOME_PERSONAGEM_REGEX = "^[a-zA-ZÀ-ÿ0-9\\s'-]+$";
        public static final String TELEFONE_REGEX = "^\\+?[1-9]\\d{1,14}$";
        public static final String URL_REGEX = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";

        private Formato() {}
    }

    // ===== LIMITES DE TAMANHO =====
    public static final class Limites {
        // Usuário
        public static final int USUARIO_EMAIL_MAX = 255;
        public static final int USUARIO_NOME_MIN = 2;
        public static final int USUARIO_NOME_MAX = 100;
        public static final int USUARIO_IMAGEM_URL_MAX = 500;
        public static final int USUARIO_PROVIDER_MAX = 50;
        public static final int USUARIO_PROVIDER_ID_MAX = 255;

        // Ficha
        public static final int FICHA_NOME_MIN = 3;
        public static final int FICHA_NOME_MAX = 100;
        public static final int FICHA_TITULO_MAX = 200;
        public static final int FICHA_INSOLITUS_MAX = 200;
        public static final int FICHA_ORIGEM_MAX = 200;
        public static final int FICHA_GENERO_MAX = 20;
        public static final int FICHA_ARQUETIPO_MAX = 200;
        public static final int FICHA_COR_MAX = 50;
        public static final int FICHA_JOGADOR_NOME_MAX = 100;
        public static final int FICHA_CLASSE_MAX = 50;
        public static final int FICHA_RACA_MAX = 50;
        public static final int FICHA_HISTORIA_MAX = 2000;
        public static final int FICHA_NIVEL_MIN = 1;
        public static final int FICHA_NIVEL_MAX = 20;

        // Atributos RPG
        public static final int ATRIBUTO_MIN = 1;
        public static final int ATRIBUTO_MAX = 20;

        // Jogo
        public static final int JOGO_NOME_MIN = 3;
        public static final int JOGO_NOME_MAX = 200;
        public static final int JOGO_DESCRICAO_MAX = 5000;
        public static final int JOGO_IMAGEM_URL_MAX = 2000;

        // Classe Personagem
        public static final int CLASSE_NOME_MIN = 2;
        public static final int CLASSE_NOME_MAX = 100;
        public static final int CLASSE_DESCRICAO_MAX = 2000;

        // Raça
        public static final int RACA_NOME_MIN = 2;
        public static final int RACA_NOME_MAX = 100;
        public static final int RACA_DESCRICAO_MAX = 2000;

        private Limites() {}
    }
}
