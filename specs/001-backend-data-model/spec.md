# Feature Specification: Reestruturação Backend - Modelagem de Dados e API Klayrah RPG

**Feature Branch**: `001-backend-data-model`  
**Created**: 2026-02-01  
**Status**: Draft  
**Input**: Análise completa do sistema Klayrah RPG (migração de React para Angular + Spring Boot)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Autenticação e Gerenciamento de Usuários (Priority: P1)

Um usuário deve poder se autenticar via OAuth2 (Google) e ter seu perfil criado/atualizado automaticamente no sistema. O sistema deve distinguir entre Mestres (admin) e Jogadores (user).

**Why this priority**: Sem autenticação, nenhuma outra funcionalidade pode ser acessada de forma segura. É a base de todo o sistema.

**Independent Test**: Pode ser testado fazendo login via Google e verificando que o usuário foi criado no banco com role correto.

**Acceptance Scenarios**:

1. **Given** usuário não cadastrado, **When** faz login via Google OAuth2, **Then** um novo registro de usuário é criado com role JOGADOR
2. **Given** usuário já cadastrado, **When** faz login via Google OAuth2, **Then** seus dados são atualizados (nome, imagem)
3. **Given** usuário autenticado, **When** acessa endpoint protegido, **Then** tem acesso permitido
4. **Given** usuário não autenticado, **When** acessa endpoint protegido, **Then** recebe 401 Unauthorized

---

### User Story 2 - CRUD de Jogos/Campanhas (Priority: P1)

Um Mestre deve poder criar jogos/campanhas e convidar jogadores. Jogadores podem participar de múltiplos jogos.

**Why this priority**: O Jogo é a entidade raiz que organiza todas as fichas e participantes. Sem ele, não há contexto para as fichas.

**Independent Test**: Mestre cria um jogo, convida jogadores, jogadores aceitam convite e aparecem como participantes.

**Acceptance Scenarios**:

1. **Given** usuário é Mestre, **When** cria um novo jogo com nome e descrição, **Then** jogo é criado e mestre é automaticamente adicionado como participante
2. **Given** jogo existe, **When** mestre convida jogador por email, **Then** convite é enviado e jogador pode aceitar
3. **Given** jogador recebeu convite, **When** aceita convite, **Then** é adicionado como participante do jogo
4. **Given** usuário é Jogador, **When** tenta criar jogo, **Then** recebe 403 Forbidden
5. **Given** mestre do jogo, **When** remove jogador, **Then** jogador perde acesso ao jogo e suas fichas

---

### User Story 3 - CRUD Completo de Fichas de Personagem (Priority: P1)

Um jogador deve poder criar, visualizar, editar e excluir suas próprias fichas de personagem dentro de um jogo. O Mestre pode visualizar e editar todas as fichas do jogo.

**Why this priority**: Fichas são o core do produto - sem elas não há sistema de RPG.

**Independent Test**: Jogador cria ficha, edita atributos, visualiza ficha formatada, exclui ficha.

**Acceptance Scenarios**:

1. **Given** jogador participa do jogo, **When** cria nova ficha, **Then** ficha é criada com valores padrão e associada ao jogador e jogo
2. **Given** ficha existe, **When** jogador edita atributos, **Then** cálculos derivados (ímpeto, bônus) são recalculados automaticamente
3. **Given** ficha existe, **When** jogador altera experiência, **Then** nível é recalculado automaticamente baseado na tabela de XP
4. **Given** mestre do jogo, **When** acessa ficha de qualquer jogador, **Then** pode visualizar e editar
5. **Given** jogador, **When** tenta acessar ficha de outro jogador, **Then** recebe 403 Forbidden
6. **Given** ficha existe, **When** dono exclui ficha, **Then** ficha é marcada como inativa (soft delete)

---

### User Story 4 - Sistema de Vida e Dano por Membro (Priority: P2)

A ficha deve calcular automaticamente a vida total e rastrear danos por cada membro do corpo (cabeça, tronco, braços, pernas, sangue).

**Why this priority**: Importante para gameplay, mas o sistema funciona minimamente sem isso.

**Independent Test**: Criar ficha, verificar cálculo de vida, registrar dano em membro específico.

**Acceptance Scenarios**:

1. **Given** ficha com vigor 20, nível 5, **When** calcula vida total, **Then** vida = Vigor + Nível + Vantagens + Renascimentos + Outros
2. **Given** vida total = 100, **When** consulta vida do tronco, **Then** retorna 100 (100% da vida)
3. **Given** vida total = 100, **When** consulta vida da cabeça, **Then** retorna 75 (75% da vida)
4. **Given** membro com 50 de vida base, dano = 20, **When** consulta vida atual, **Then** retorna 30

---

### User Story 5 - Sistema de Essência e Ameaça (Priority: P2)

A ficha deve calcular automaticamente Essência (recurso mágico) e Ameaça (nível de perigo).

**Why this priority**: Complementa o sistema de vida para mecânicas avançadas de RPG.

**Independent Test**: Criar ficha, verificar cálculo de essência e ameaça baseados nos atributos.

**Acceptance Scenarios**:

1. **Given** ficha com Vigor=20, Sabedoria=30, Nível=5, **When** calcula essência, **Then** essência = (Vigor+Sabedoria)/2 + Nível + Renascimentos + Vantagens + Outros
2. **Given** essência total = 50, gastos = 20, **When** consulta essência restante, **Then** retorna 30
3. **Given** ficha nível 10, **When** calcula ameaça, **Then** ameaça = Nível + Itens + Títulos + Renascimentos + Outros

---

### User Story 6 - Galeria de Imagens do Personagem e Itens (Priority: P3)

Jogadores podem fazer upload de imagens do personagem e de seus itens, com análise opcional via IA.

**Why this priority**: Funcionalidade de enriquecimento, não essencial para o funcionamento básico.

**Independent Test**: Upload de imagem, visualização em galeria, exclusão de imagem.

**Acceptance Scenarios**:

1. **Given** ficha existe, **When** jogador faz upload de imagem (≤20MB), **Then** imagem é salva e associada à ficha
2. **Given** imagem existe, **When** solicita análise IA, **Then** descrição é gerada e salva
3. **Given** jogador, **When** tenta upload >20MB, **Then** recebe erro de validação
4. **Given** imagem existe, **When** dono exclui, **Then** imagem é removida

---

### User Story 7 - Sistema de Anotações (Priority: P3)

Jogadores podem criar anotações associadas às suas fichas.

**Why this priority**: Funcionalidade auxiliar, não impacta mecânicas de jogo.

**Independent Test**: Criar nota, listar notas, excluir nota.

**Acceptance Scenarios**:

1. **Given** ficha existe, **When** jogador cria anotação, **Then** anotação é salva com timestamp
2. **Given** anotações existem, **When** lista anotações, **Then** retorna ordenado por data (mais recente primeiro)
3. **Given** anotação existe, **When** dono exclui, **Then** anotação é removida

---

### User Story 8 - Fichas de NPCs para Mestres (Priority: P2)

Mestres podem criar fichas de NPCs (Non-Player Characters) que pertencem ao jogo, não a um jogador específico.

**Why this priority**: Essencial para mestres conduzirem o jogo com personagens controlados.

**Independent Test**: Mestre cria NPC, edita NPC, jogadores não podem editar NPCs.

**Acceptance Scenarios**:

1. **Given** mestre do jogo, **When** cria ficha como NPC, **Then** ficha é criada sem jogador associado, apenas ao jogo
2. **Given** NPC existe, **When** jogador tenta editar, **Then** recebe 403 Forbidden
3. **Given** jogo tem NPCs, **When** mestre lista fichas, **Then** vê todas as fichas incluindo NPCs

---

### Edge Cases

- O que acontece quando um jogador é removido de um jogo? Suas fichas devem ser arquivadas, não excluídas.
- Como tratar quando mestre tenta deletar a si mesmo do jogo? Deve ser impedido se for o único mestre.
- O que acontece se a API do Gemini falhar na análise de imagem? Retornar erro gracioso e permitir retry.
- Como lidar com upload de imagem com formato inválido? Validar MIME type e rejeitar.
- O que acontece quando XP excede o nível máximo configurado? Aplicar status "Renascimento" conforme config.

## Requirements *(mandatory)*

### Functional Requirements

#### Autenticação e Autorização
- **FR-001**: Sistema MUST autenticar usuários via OAuth2 (Google)
- **FR-002**: Sistema MUST criar registro de usuário automaticamente no primeiro login
- **FR-003**: Sistema MUST suportar dois roles por jogo: MESTRE e JOGADOR
- **FR-004**: Sistema MUST permitir que um usuário seja MESTRE em alguns jogos e JOGADOR em outros

#### Jogos/Campanhas
- **FR-005**: Sistema MUST permitir que qualquer usuário crie jogos (tornando-se MESTRE)
- **FR-006**: Sistema MUST permitir convite de jogadores por email
- **FR-007**: Sistema MUST rastrear participantes (JogoParticipante) com role por jogo
- **FR-008**: Sistema MUST impedir que jogadores sem convite acessem jogos

#### Configuração do Jogo (NOVO - Mestre)
- **FR-030**: Sistema MUST permitir que MESTRE configure atributos customizados por jogo
- **FR-031**: Sistema MUST permitir que MESTRE configure níveis, XP e limitadores por jogo
- **FR-032**: Sistema MUST permitir que MESTRE configure aptidões e tipos de aptidão por jogo
- **FR-033**: Sistema MUST permitir que MESTRE configure bônus e suas fórmulas por jogo
- **FR-034**: Sistema MUST permitir que MESTRE configure membros do corpo e porcentagens por jogo
- **FR-035**: Sistema MUST permitir que MESTRE cadastre classes com bônus por nível
- **FR-036**: Sistema MUST permitir que MESTRE cadastre raças com bônus de atributo
- **FR-037**: Sistema MUST permitir que MESTRE aplique template "Klayrah Padrão" ao criar jogo
- **FR-038**: Sistema MUST impedir criação de fichas em jogos sem configuração mínima

#### Histórico de Alterações (NOVO)
- **FR-040**: Sistema MUST registrar todas alterações em fichas (auditoria)
- **FR-041**: Sistema MUST armazenar: usuário, data, campo, valor anterior, valor novo
- **FR-042**: Sistema MUST permitir que MESTRE visualize histórico de qualquer ficha do jogo
- **FR-043**: Sistema MUST impedir que JOGADOR acesse histórico

#### Fichas de Personagem
- **FR-009**: Sistema MUST implementar CRUD completo de fichas
- **FR-010**: Sistema MUST calcular automaticamente campos derivados usando configurações do jogo
- **FR-011**: Sistema MUST validar limite de atributos baseado no Limitador configurado
- **FR-012**: Sistema MUST rastrear distribuição de pontos de atributo por nível
- **FR-013**: Sistema MUST suportar N atributos configuráveis (padrão: 7)
- **FR-014**: Sistema MUST suportar N aptidões configuráveis (padrão: 24)
- **FR-015**: Sistema MUST suportar N bônus calculados configuráveis (padrão: 6)
- **FR-016**: Sistema MUST suportar N membros do corpo configuráveis (padrão: 7)
- **FR-017**: Sistema MUST implementar soft delete para fichas

#### NPCs
- **FR-018**: Sistema MUST permitir que mestres criem fichas de NPC (sem jogador associado)
- **FR-019**: Sistema MUST restringir edição de NPCs apenas a mestres

#### Galerias e Anotações
- **FR-020**: Sistema MUST suportar upload de imagens (≤20MB) para personagem e itens
- **FR-021**: Sistema MUST armazenar imagens em storage (S3 ou local)
- **FR-022**: Sistema MUST suportar anotações por ficha com timestamp
- **FR-023**: Sistema SHOULD integrar com API Gemini para análise de imagens (opcional)

#### Exportação e IA (NOVO - do legado)
- **FR-050**: Sistema MUST permitir exportação de ficha em PDF
- **FR-051**: Sistema MUST incluir imagem do personagem no PDF (se existir)
- **FR-052**: Sistema MUST incluir todos os atributos, aptidões e cálculos no PDF
- **FR-053**: Sistema SHOULD permitir sugestão de interpretação via IA (Gemini)
- **FR-054**: Sistema MUST validar pontos de atributo distribuídos vs esperados
- **FR-055**: Sistema MUST retornar warnings de validação junto com dados da ficha

### Technical Requirements (NOVO)

- **TR-001**: Sistema MUST NOT usar colunas JSON no banco de dados
- **TR-002**: Sistema MUST usar MapStruct para mapeamento DTO ↔ Entity
- **TR-003**: Sistema MUST NOT usar JPA Converters
- **TR-004**: Sistema MUST usar Hibernate Envers para auditoria
- **TR-005**: Sistema MUST usar mínimo de Enums (apenas RoleJogo, TipoGaleria)
- **TR-006**: Sistema MUST normalizar todos os dados em tabelas separadas

### Non-Functional Requirements

- **NFR-001**: API MUST responder em <200ms para operações simples (p95)
- **NFR-002**: Sistema MUST suportar até 1000 usuários simultâneos
- **NFR-003**: Imagens MUST ser validadas e sanitizadas antes do armazenamento
- **NFR-004**: Todos os endpoints MUST ter documentação OpenAPI
- **NFR-005**: Sistema MUST implementar rate limiting (Bucket4j)
- **NFR-006**: Sistema MUST cachear configurações do jogo (mudam raramente)

### Key Entities (ATUALIZADO)

#### Entidades Principais
- **Usuario**: Usuário do sistema, autenticado via OAuth2
- **Jogo**: Campanha/sessão de RPG com suas configurações
- **JogoParticipante**: Associação Usuario-Jogo com role (MESTRE/JOGADOR)
- **Ficha**: Ficha de personagem com referências às configurações

#### Entidades de Configuração (definidas pelo Mestre)
- **AtributoConfig**: Configuração de atributo (nome, fórmula de ímpeto)
- **NivelConfig**: Configuração de nível (XP, limitador, pontos)
- **TipoAptidao**: Categoria de aptidão (Física, Mental, etc.)
- **AptidaoConfig**: Configuração de aptidão
- **BonusConfig**: Configuração de bônus (nome, fórmula base)
- **MembroCorpoConfig**: Configuração de membro (nome, porcentagem)
- **ClassePersonagem**: Classe cadastrada com seus bônus
- **Raca**: Raça cadastrada com seus bônus de atributo
- **DadoProspeccaoConfig**: Dados de prospecção disponíveis (d3, d4, d6, etc.)
- **CategoriaVantagem**: Categoria de vantagem (Treinamento Físico, Mental, Ação, Reação, etc.)
- **VantagemConfig**: Vantagem cadastrada com custo, níveis, pré-requisitos e efeitos
- **VantagemPreRequisito**: Pré-requisitos de uma vantagem
- **VantagemEfeito**: Efeitos/bônus que uma vantagem concede

#### Entidades de Dados da Ficha
- **FichaAtributo**: Valor de atributo na ficha
- **FichaAptidao**: Valor de aptidão na ficha
- **FichaBonus**: Valor de bônus na ficha
- **FichaVida**: Sistema de vida da ficha
- **FichaVidaMembro**: Dano por membro
- **FichaEssencia**: Sistema de essência da ficha
- **FichaAmeaca**: Sistema de ameaça da ficha
- **FichaProspeccao**: Contador de prospecção por dado
- **FichaVantagem**: Vantagem comprada para a ficha (com nível atual)
- **Anotacao**: Nota de texto associada a uma ficha

#### Entidades Futuras (não implementar agora)
- ~~ImagemGaleria~~: Upload de imagens (FUTURO)
