# Implementation Plan: Reestruturação Backend - Modelagem de Dados e API Klayrah RPG

**Branch**: `001-backend-data-model` | **Date**: 2026-02-01 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-backend-data-model/spec.md`
**Revisado**: 2026-02-01 - Sistema totalmente configurável pelo Mestre

## Summary

Reestruturação completa do backend do sistema Klayrah RPG com foco em:

1. **Sistema totalmente configurável pelo Mestre** - Atributos, aptidões, níveis, classes, raças, bônus
2. **Banco de dados normalizado** - Sem colunas JSON, tudo em tabelas separadas
3. **MapStruct para mapeamento** - Sem JPA Converters
4. **Histórico de alterações** - Hibernate Envers para auditoria completa
5. **Mínimo de Enums** - Apenas tipos fixos do sistema (RoleJogo, TipoGaleria)

## Technical Context

**Language/Version**: Java 25  
**Framework**: Spring Boot 4.0.2  
**Primary Dependencies**: 
- Spring Web, Spring Security, Spring Data JPA
- OAuth2 Client/Resource Server
- Bucket4j (Rate Limiting)
- SpringDoc OpenAPI
- MapStruct (Mapeamento DTO ↔ Entity)
- Hibernate Envers (Auditoria)

**Storage**: PostgreSQL (normalizado, sem JSON columns)  
**Testing**: JUnit 5, Mockito, Spring Boot Test  
**Target Platform**: Linux server (Docker/AWS)  
**Project Type**: Web application (backend API)  
**Performance Goals**: <200ms p95 para operações simples, 1000 usuários simultâneos  
**Constraints**: 
- Soft delete para dados sensíveis
- Rate limiting
- Validação completa
- Auditoria obrigatória
- Sem JSON columns
- Sem JPA Converters

**Scale/Scope**: ~33 tabelas, ~60 endpoints, ~150 testes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Clean Architecture | ✅ PASS | Camadas bem definidas |
| II. RESTful API Standards | ✅ PASS | Versionamento /api/v1/, OpenAPI |
| III. Security First | ✅ PASS | OAuth2, rate limiting, auditoria |
| IV. Test-First Mindset | ✅ PASS | Estrutura de testes definida |
| V. Domain-Driven Design | ✅ PASS | Entidades ricas, configuração por jogo |
| VI. Simplicity & YAGNI | ✅ PASS | Configurável mas não over-engineered |

## Project Structure

### Documentation (this feature)

```text
specs/001-backend-data-model/
├── plan.md              # This file
├── research.md          # Phase 0 output ✅
├── data-model.md        # Phase 1 output ✅
├── quickstart.md        # Phase 1 output (pendente)
├── contracts/           # Phase 1 output (pendente)
│   ├── auth-api.yaml
│   ├── jogos-api.yaml
│   ├── config-api.yaml
│   └── fichas-api.yaml
└── tasks.md             # Phase 2 output
```

### Source Code (repository root)

```text
src/main/java/br/com/hydroom/rpg/fichacontrolador/
├── FichaControladorApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   ├── CorsConfig.java
│   ├── RateLimitConfig.java
│   ├── AuditConfig.java          # Hibernate Envers config
│   └── MapStructConfig.java      # MapStruct config
├── constants/
│   └── ValidationMessages.java
├── controller/
│   ├── AuthController.java
│   ├── UsuarioController.java
│   ├── JogoController.java
│   ├── FichaController.java
│   ├── GaleriaController.java
│   ├── AnotacaoController.java
│   └── config/                   # Controllers de configuração (Mestre)
│       ├── AtributoConfigController.java
│       ├── NivelConfigController.java
│       ├── AptidaoConfigController.java
│       ├── BonusConfigController.java
│       ├── MembroCorpoConfigController.java
│       ├── ClasseController.java
│       ├── RacaController.java
│       └── HistoricoController.java
├── dto/
│   ├── request/
│   │   ├── CriarJogoRequest.java
│   │   ├── CriarFichaRequest.java
│   │   ├── AtualizarFichaRequest.java
│   │   ├── CriarAtributoConfigRequest.java
│   │   └── ...
│   └── response/
│       ├── UsuarioResponse.java
│       ├── JogoResponse.java
│       ├── FichaResponse.java
│       ├── FichaResumoResponse.java
│       ├── AtributoConfigResponse.java
│       ├── HistoricoResponse.java
│       └── ...
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── AccessDeniedException.java
│   └── BusinessException.java
├── filter/
│   └── RateLimitFilter.java
├── mapper/                        # MapStruct Mappers
│   ├── UsuarioMapper.java
│   ├── JogoMapper.java
│   ├── FichaMapper.java
│   ├── AtributoConfigMapper.java
│   ├── AptidaoConfigMapper.java
│   ├── ClasseMapper.java
│   ├── RacaMapper.java
│   └── ...
├── model/
│   ├── Usuario.java
│   ├── Jogo.java
│   ├── JogoParticipante.java
│   ├── Ficha.java
│   ├── ImagemGaleria.java
│   ├── Anotacao.java
│   ├── config/                    # Entidades de configuração
│   │   ├── AtributoConfig.java
│   │   ├── NivelConfig.java
│   │   ├── TipoAptidao.java
│   │   ├── AptidaoConfig.java
│   │   ├── BonusConfig.java
│   │   ├── MembroCorpoConfig.java
│   │   ├── ClassePersonagem.java
│   │   ├── ClasseBonus.java
│   │   ├── ClasseAptidaoBonus.java
│   │   ├── Raca.java
│   │   ├── RacaAtributoBonus.java
│   │   ├── RacaClassePermitida.java
│   │   ├── EssenciaConfig.java
│   │   └── AmeacaConfig.java
│   ├── ficha/                     # Entidades de dados da ficha
│   │   ├── FichaAtributo.java
│   │   ├── FichaAptidao.java
│   │   ├── FichaBonus.java
│   │   ├── FichaVida.java
│   │   ├── FichaVidaMembro.java
│   │   ├── FichaEssencia.java
│   │   └── FichaAmeaca.java
│   ├── audit/                     # Auditoria customizada
│   │   └── CustomRevisionEntity.java
│   └── enums/                     # Apenas enums fixos
│       ├── RoleJogo.java
│       ├── TipoGaleria.java
│       └── TipoAlteracao.java
├── repository/
│   ├── UsuarioRepository.java
│   ├── JogoRepository.java
│   ├── JogoParticipanteRepository.java
│   ├── FichaRepository.java
│   ├── ImagemGaleriaRepository.java
│   ├── AnotacaoRepository.java
│   ├── config/
│   │   ├── AtributoConfigRepository.java
│   │   ├── NivelConfigRepository.java
│   │   └── ...
│   └── ficha/
│       ├── FichaAtributoRepository.java
│       └── ...
├── service/
│   ├── UsuarioService.java
│   ├── JogoService.java
│   ├── FichaService.java
│   ├── GaleriaService.java
│   ├── AnotacaoService.java
│   ├── CalculoFichaService.java
│   ├── HistoricoService.java
│   ├── TemplateJogoService.java   # Cria configuração padrão Klayrah
│   ├── config/
│   │   ├── AtributoConfigService.java
│   │   ├── NivelConfigService.java
│   │   └── ...
│   └── GeminiService.java (opcional)
└── util/
    └── FormulaParser.java         # Parser para fórmulas de cálculo

src/test/java/br/com/hydroom/rpg/fichacontrolador/
├── mapper/
│   └── FichaMapperTest.java
├── controller/
│   ├── AuthControllerTest.java
│   ├── JogoControllerTest.java
│   ├── FichaControllerTest.java
│   └── config/
│       └── AtributoConfigControllerTest.java
├── service/
│   ├── UsuarioServiceTest.java
│   ├── JogoServiceTest.java
│   ├── FichaServiceTest.java
│   ├── CalculoFichaServiceTest.java
│   └── TemplateJogoServiceTest.java
├── repository/
│   └── ...
└── integration/
    ├── AuthIntegrationTest.java
    ├── JogoIntegrationTest.java
    ├── FichaIntegrationTest.java
    └── ConfigIntegrationTest.java
```

**Structure Decision**: Modelo normalizado com tabelas de configuração (`config/`) separadas das tabelas de dados da ficha (`ficha/`). MapStruct para todos os mapeamentos DTO ↔ Entity. Hibernate Envers para auditoria automática.

## Complexity Tracking

> **No violations identified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Muitas tabelas (~33) | Configurabilidade total requerida | JSON columns rejeitadas por requisito |
| Hibernate Envers | Histórico obrigatório para Mestre | Custom audit seria mais trabalho |

## Key Technical Decisions

### 1. MapStruct ao invés de Converters

```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    
    @Mapping(source = "usuario.nome", target = "nomeJogador")
    @Mapping(source = "jogo.nome", target = "nomeJogo")
    @Mapping(source = "classe.nome", target = "nomeClasse")
    @Mapping(source = "raca.nome", target = "nomeRaca")
    FichaResponse toResponse(Ficha ficha);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    Ficha toEntity(CriarFichaRequest request);
}
```

### 2. Hibernate Envers para Auditoria

```java
@Entity
@Audited
@Table(name = "fichas")
public class Ficha {
    // Todas alterações serão auditadas automaticamente
}

@Entity
@RevisionEntity(CustomRevisionListener.class)
public class CustomRevisionEntity {
    @Id
    @GeneratedValue
    @RevisionNumber
    private Long rev;
    
    @RevisionTimestamp
    private Long timestamp;
    
    private Long usuarioId;
    private String ipOrigem;
}
```

### 3. Configuração por Jogo

Cada jogo tem configurações independentes:
- Atributos, aptidões, níveis são copiados do template ou criados do zero
- Permite diferentes sistemas de RPG no mesmo sistema

### 4. Template Klayrah Padrão

```java
@Service
public class TemplateJogoService {
    public void aplicarTemplateKlayrah(Long jogoId) {
        criarAtributosPadrao(jogoId);  // Força, Agilidade, etc.
        criarAptidoesPadrao(jogoId);   // 24 aptidões
        criarNiveisPadrao(jogoId);     // 0-35 níveis
        criarClassesPadrao(jogoId);    // Guerreiro, Mago, etc.
        criarMembrosPadrao(jogoId);    // Cabeça, Tronco, etc.
        criarBonusPadrao(jogoId);      // BBA, Bloqueio, etc.
    }
}
```

---

## Phase 0: Research ✅ COMPLETO

Ver [research.md](./research.md)

## Phase 1: Design ✅ PARCIALMENTE COMPLETO

### Completo:
- [x] data-model.md - Modelo de dados normalizado
- [x] research.md - Decisões técnicas

### Pendente:
- [ ] contracts/ - OpenAPI specs
- [ ] quickstart.md - Guia de desenvolvimento

---

**Next Steps**:
1. ✅ research.md - Completo
2. ✅ data-model.md - Completo  
3. ⏳ Criar contracts/ com OpenAPI specs
4. ⏳ Criar quickstart.md
5. ⏳ Atualizar agent context
6. ⏳ Phase 2: tasks.md
