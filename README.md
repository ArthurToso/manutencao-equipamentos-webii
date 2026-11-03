# manutencao-equipamentos-webii

Trabalho da disciplina de Desenvolvimento Web 2


## Testar o envio de e-mail (autocadastro)

O projeto usa o Mailpit, um servidor de e-mail para testes que sobe junto com o banco. Os e-mails não são entregues de verdade: ficam guardados no Mailpit para consulta.

1. Na raiz do projeto, suba o banco e o Mailpit:
   docker compose up -d
2. Suba o backend e o frontend normalmente.
3. Acesse a tela de cadastro e cadastre um cliente novo (CPF e e-mail que ainda não existam no sistema).
4. Abra http://localhost:8025 no navegador. O e-mail com a senha de 4 dígitos aparece na caixa de entrada.
5. Faça login com o e-mail cadastrado e a senha recebida.

Se o e-mail não aparecer, confira se o Mailpit está rodando com `docker compose ps`.
