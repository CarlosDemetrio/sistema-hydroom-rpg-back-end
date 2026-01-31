# Estrutura de Camadas

- `com.projeto.api`: Controllers e Exception Handlers.
- `com.projeto.domain.service`: Lógica de negócio pura.
- `com.projeto.domain.model`: Entidades JPA e Enums.
- `com.projeto.infrastructure.repository`: Interfaces Spring Data JPA.
- `com.projeto.dto`: Records para transferência de dados.
- `com.projeto.config`: Configurações de Bean, Security e AWS.

### Padrão de Resposta
Todas as APIs devem seguir o padrão RESTful e retornar códigos HTTP apropriados (201 para criação, 204 para deleção, etc).
