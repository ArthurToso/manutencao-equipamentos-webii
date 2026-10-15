# Decisão Arquitetural: Autenticação com JWT Stateless

## 1. Contexto
O sistema "Controle de Manutenção de Equipamentos" possui arquitetura desacoplada, composta por um frontend em Angular (SPA) e uma API REST em Spring Boot. Era necessário definir a estratégia de autenticação e autorização para identificar clientes e funcionários com diferentes níveis de permissão.

## 2. Decisão
Adotar autenticação **stateless** utilizando **JSON Web Tokens (JWT)** assinados com o algoritmo simétrico **HMAC-SHA256 (HS256)**.
- O token armazena no subject o identificador do usuário (`usuarioId`) e carrega como claims públicas não confidenciais: `email`, `perfil` (`CLIENTE` ou `FUNCIONARIO`), e `clienteId` ou `funcionarioId`.
- Nunca são incluídos no token dados sensíveis (senhas, hashes ou salts).
- O backend valida a assinatura e a expiração do token em cada requisição protegida através do filtro `JwtAuthenticationFilter`, montando o contexto do Spring Security de forma independente para cada requisição.

## 3. Alternativa Descartada
**Sessão HTTP Stateful com Cookies (JSESSIONID)**:
- *Motivo do descarte*: Sessões no servidor demandam persistência de estado (memória ou Redis), dificultam a escalabilidade horizontal e introduzem complexidade na configuração de CORS e proteção CSRF para clientes SPA em origens distintas durante desenvolvimento e deploy.

## 4. Motivo da Escolha
- **Desacoplamento e Padrão REST**: Mantém o servidor completamente stateless, eliminando o consumo de memória de sessão para cada usuário conectado.
- **Integração com Angular**: Fácil inclusão do header `Authorization: Bearer <token>` via interceptor HTTP no cliente.
- **Autocontenção**: Os claims do JWT permitem que o backend identifique o perfil e o ID de domínio (`clienteId`/`funcionarioId`) diretamente a partir do token verificado criptograficamente.

## 5. Consequências
- **Logout no Frontend**: O logout é realizado exclusivamente no lado do cliente através da exclusão do token armazenado (`localStorage`/`sessionStorage`).
- **Ausência de Lista de Revogação**: Como não há blacklist centralizada em banco para preservar a característica stateless, um token válido continua ativo até seu tempo de expiração.
- **Tempo de Expiração Fixo**: Para mitigar riscos de segurança decorrentes da ausência de revogação, o tempo de vida do token foi fixado em **120 minutos (2 horas)**.

## 6. Suposições
- Não há suporte a **Refresh Token** nesta fase do projeto. O usuário deve realizar novo login após a expiração.
- A chave secreta (`app.jwt.secret`) é injetada por variável de ambiente (`JWT_SECRET`) com valor padrão seguro (mínimo de 256 bits) para o ambiente de desenvolvimento.
- As senhas dos usuários utilizam criptografia **SHA-256 + Salt** conforme especificação do projeto, sendo verificadas no login antes da emissão do JWT.

