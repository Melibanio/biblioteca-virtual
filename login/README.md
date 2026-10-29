# Biblioteca Virtual — Java + Spring Boot + H2

## Como executar
1. Extraia o ZIP.
2. Abra no VS Code a pasta que contém o arquivo `pom.xml`.
3. Execute `BibliotecaVirtualApplication.java`.
4. Acesse http://localhost:8080.

## O que o projeto faz
- Cadastra usuários e salva os dados no banco H2.
- Verifica se o e-mail já está cadastrado.
- Confere se a senha e a confirmação são iguais.
- Permite entrar com e-mail e senha.
- Exibe os dados do usuário na tela de perfil.
- Permite sair da conta.
- Permite redefinir a senha pela tela de recuperação.

## Conceitos usados
- Spring Boot para iniciar a aplicação.
- Controller para receber os formulários.
- Repository para consultar e salvar usuários.
- JPA para relacionar a classe Usuario com a tabela do banco.
- H2 como banco de dados.

