# Documentação Semanal

## Data: 02/09/2026

**Requisito/Tarefa desenvolvida**:
[Backend] RF004 - endpoint para criar solicitação de manutenção (estado ABERTA)

**Descrição do que foi implementado**:
Criamos o projeto base do backend usando Spring Boot e implementamos o endpoint necessário para que o cliente possa registrar uma solicitação de manutenção. Como ainda não tínhamos a estrutura de backend no repositório, configuramos o projeto do zero com Maven, banco de dados PostgreSQL e Spring Data JPA.

**Funcionalidades adicionadas**:
- Estrutura inicial do projeto Spring Boot.
- Criação do endpoint POST `/api/solicitacoes` para registrar a manutenção.
- Configuração de CORS para permitir requisições do frontend Angular.

**Endpoints criados ou modificados**:
- Criado: `POST /api/solicitacoes`

**Arquivos criados**:
- Todo o projeto em `backend/` (pom.xml, ManutencaoApplication.java, application.properties, mvnw, etc).
- `EstadoSolicitacao.java` (Enum)
- `Categoria.java` (Model)
- `Solicitacao.java` (Model)
- `CategoriaRepository.java` (Repository)
- `SolicitacaoRepository.java` (Repository)
- `SolicitacaoRequest.java` (DTO)
- `SolicitacaoResponse.java` (DTO)
- `SolicitacaoService.java` (Service)
- `SolicitacaoController.java` (Controller)

**Arquivos modificados**:
- Nenhum arquivo existente foi modificado, apenas novos arquivos criados para o backend.

**Alterações de frontend**:
- Nenhuma alteração no frontend nesta tarefa.

**Alterações de backend**:
- Criação completa do projeto e do fluxo de cadastro de solicitação (Controller -> Service -> Repository -> BD).

**Dependências adicionadas**:
- No `pom.xml`: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `postgresql` (driver) e `spring-boot-starter-test`.

**Validações implementadas**:
- Usamos anotações do Jakarta Validation (`@NotBlank`, `@NotNull`) no DTO `SolicitacaoRequest` para garantir que o cliente envie os dados corretamente (descrição do equipamento, defeito, categoria e clienteId).

**Testes realizados**:
- O projeto backend foi compilado com sucesso e os erros iniciais de dependências do Maven foram corrigidos.

**Observações importantes**:
- Como ainda não temos o fluxo de login funcional no backend, o endpoint está recebendo o `clienteId` diretamente no corpo da requisição. Isso será ajustado no futuro quando a autenticação estiver finalizada.
