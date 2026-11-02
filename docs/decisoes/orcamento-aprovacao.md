# Decisões de Arquitetura e Suposições: Orçamento e Aprovação de Serviço (RF005 e RF006)

Este documento registra as decisões técnicas e suposições adotadas para o desenvolvimento dos requisitos **RF005 (Mostrar Orçamento)** e **RF006 (Aprovar Serviço)** no sistema de Manutenção de Equipamentos (Web II - UFPR/TADS).

---

## 1. Contexto e Escopo

- **RF005 (Mostrar Orçamento)**: O cliente visualiza todos os dados de sua solicitação e o valor orçado em destaque.
- **RF006 (Aprovar Serviço)**: O cliente aprova o serviço a partir do estado `ORCADA`. O sistema transita o estado para `APROVADA`, grava histórico e apresenta mensagem com o valor do serviço aprovado antes de redirecionar para a tela inicial do cliente (`/cliente/home`, RF003).

---

## 2. Suposições e Decisões de Negócio

1. **Ações permitidas exclusivamente no estado `ORCADA`**:
   - A tela de orçamento (`/cliente/orcamento/:id`) somente apresenta botões de decisão ("Aprovar Serviço" e "Rejeitar Serviço") se a solicitação estiver com estado `ORCADA`.
   - Caso o cliente acerte ou recarregue a URL de uma solicitação em outro estado (ex.: já aprovada, rejeitada, arrumada, etc.), a tela permanece em modo somente-leitura, omitindo os botões de ação e exibindo o aviso informativo: *"Este orçamento não está aguardando a sua decisão"*.

2. **Leitura de orçamento em solicitações não orçadas (`ABERTA` ou valor nulo)**:
   - A rota `GET /api/solicitacoes/{id}/orcamento` valida se a solicitação já recebeu orçamento. Se estiver em estado `ABERTA` ou sem valor definido, o backend retorna **HTTP 409 Conflict** com a mensagem *"A solicitação ainda não foi orçada"*.
   - Para os demais estados após o orçamento (`ORCADA`, `APROVADA`, `REJEITADA`, etc.), a consulta de leitura do orçamento é permitida.

3. **Validação estrita de posse (Cliente Dono)**:
   - Apenas o cliente proprietário da solicitação (`solicitacao.clienteId == usuarioLogado.clienteId`) pode consultar o orçamento e aprovar o serviço.
   - Tentativas de acesso ou aprovação de solicitações pertencentes a outro cliente retornam **HTTP 403 Forbidden** (ou 401 se não autenticado).

4. **Mensagem de aprovação baseada na confirmação do servidor**:
   - A mensagem de sucesso exibida no modal (*"Serviço Aprovado no Valor R$ xx,xx"*) utiliza o valor retornado na resposta do backend (`SolicitacaoResponse.valorOrcamento`), garantindo que o cliente veja exatamente o valor validado e persistido no banco de dados.

5. **Proteção contra Duplo Clique e Concorrência**:
   - **Frontend**: Ao acionar "Aprovar Serviço", o botão é desabilitado imediatamente e exibe *"Aprovando…"*, impedindo submissões duplicadas na mesma sessão.
   - **Backend**: A transição para `APROVADA` passa pelo ponto único de mudança de estado (`SolicitacaoService.alterarEstado`), garantindo validação transacional (`@Transactional`). Se o estado já não for mais `ORCADA` (por exemplo, aprovação prévia em outra aba ou requisição concorrente), o backend retorna **HTTP 409 Conflict** com a mensagem *"Só é possível aprovar uma solicitação orçada"*.
   - Em caso de 409 no frontend, os dados são automaticamente recarregados, atualizando a tela para o estado consistente e removendo os botões de ação.

6. **Mapeamento de Estados e Compatibilidade de Rótulos**:
   - Para garantir consistência tipada entre a API (que trafega a string `"ORCADA"`) e o frontend, foi implementado o mapper `mapearEstadoSolicitacao`, eliminando type assertions frágeis (`as EstadoSolicitacao`) e tratando eventuais variações com e sem cedilha sem alterar os rótulos de exibição do enum.

7. **Acessibilidade do Modal de Sucesso**:
   - O modal de confirmação implementa os atributos `role="dialog"` e `aria-modal="true"`, aplicando foco automático no botão "OK" para garantir acessibilidade e conformidade com os critérios de usabilidade.

