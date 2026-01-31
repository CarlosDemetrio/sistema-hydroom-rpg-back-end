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
        public static final String NOME_PERSONAGEM_OBRIGATORIO = "Nome do personagem é obrigatório";
        public static final String NOME_PERSONAGEM_TAMANHO = "Nome deve ter entre 3 e 100 caracteres";
        public static final String NOME_PERSONAGEM_CARACTERES = "Nome contém caracteres inválidos. Use apenas letras, números, espaços, hífens e apóstrofos";

        public static final String CLASSE_OBRIGATORIA = "Classe é obrigatória";
        public static final String CLASSE_TAMANHO = "Classe deve ter no máximo 50 caracteres";

        public static final String NIVEL_MINIMO = "Nível mínimo é 1";
        public static final String NIVEL_MAXIMO = "Nível máximo é 20";

        public static final String RACA_TAMANHO = "Raça deve ter no máximo 50 caracteres";

        public static final String HISTORIA_TAMANHO = "História deve ter no máximo 2000 caracteres";

        public static final String ATRIBUTOS_TAMANHO = "Atributos excedem tamanho máximo permitido";
        public static final String HABILIDADES_TAMANHO = "Habilidades excedem tamanho máximo permitido";
        public static final String EQUIPAMENTOS_TAMANHO = "Equipamentos excedem tamanho máximo permitido";

        public static final String USUARIO_OBRIGATORIO = "Usuário é obrigatório";

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

        public static final String JSON_INVALIDO = "JSON de atributos inválido";
        public static final String ATRIBUTO_FORA_RANGE = "%s deve estar entre 1 e 20, valor recebido: %d";

        private Atributos() {}
    }

    // ===== JOGO =====
    public static final class Jogo {
        public static final String NOME_OBRIGATORIO = "Nome do jogo é obrigatório";
        public static final String NOME_TAMANHO = "Nome deve ter entre 3 e 100 caracteres";

        public static final String DESCRICAO_TAMANHO = "Descrição deve ter no máximo 1000 caracteres";

        public static final String SISTEMA_OBRIGATORIO = "Sistema de RPG é obrigatório";
        public static final String SISTEMA_TAMANHO = "Sistema deve ter no máximo 50 caracteres";

        public static final String MESTRE_OBRIGATORIO = "Mestre é obrigatório";

        public static final String MAX_JOGADORES_MINIMO = "Número mínimo de jogadores é 1";
        public static final String MAX_JOGADORES_MAXIMO = "Número máximo de jogadores é 20";

        private Jogo() {}
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

    // ===== ERROS GERAIS =====
    public static final class Erro {
        public static final String INTERNO = "Erro interno do servidor";
        public static final String NAO_ENCONTRADO = "Recurso não encontrado";
        public static final String JA_EXISTE = "Este recurso já existe";
        public static final String OPERACAO_INVALIDA = "Operação inválida";
        public static final String DADOS_INVALIDOS = "Dados inválidos";
        public static final String CONFLITO = "Conflito ao processar requisição";

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
        public static final int FICHA_CLASSE_MAX = 50;
        public static final int FICHA_RACA_MAX = 50;
        public static final int FICHA_HISTORIA_MAX = 2000;
        public static final int FICHA_JSON_MAX = 65535; // TEXT field max
        public static final int FICHA_NIVEL_MIN = 1;
        public static final int FICHA_NIVEL_MAX = 20;

        // Atributos RPG
        public static final int ATRIBUTO_MIN = 1;
        public static final int ATRIBUTO_MAX = 20;

        // Jogo
        public static final int JOGO_NOME_MIN = 3;
        public static final int JOGO_NOME_MAX = 100;
        public static final int JOGO_DESCRICAO_MAX = 1000;
        public static final int JOGO_SISTEMA_MAX = 50;
        public static final int JOGO_MAX_JOGADORES_MIN = 1;
        public static final int JOGO_MAX_JOGADORES_MAX = 20;

        private Limites() {}
    }
}
