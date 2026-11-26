# Biblioteca Virtual

Projeto acadêmico de biblioteca digital com frontend em HTML, CSS e JavaScript, backend Java, JDBC e banco H2.

## Tecnologias
- Java 17
- HTML, CSS e JavaScript puro
- H2 Database
- JDBC
- Gson
- BCrypt
- Maven

## Como executar
1. Instale o JDK 17 ou superior e o Maven.
2. Abra o terminal na pasta principal do projeto (a pasta que contém `backend` e `database`).
3. Execute `mvn -f backend/pom.xml clean compile`.
4. Execute `mvn -f backend/pom.xml exec:java`.
5. Acesse `http://localhost:8080`.

O endpoint `http://localhost:8080/api/status` verifica a disponibilidade da API e a conexão com o banco.

## Banco de dados
O banco H2 é armazenado em arquivo no caminho `database/biblioteca` por padrão. O schema é carregado automaticamente de `backend/src/main/resources/schema.sql`, criado a partir do script `database/biblioteca.sql`.

É possível alterar o caminho do banco pela propriedade Java `-Dbiblioteca.db=CAMINHO` ou pela variável de ambiente `BIBLIOTECA_DB`.

## Administrador
Por padrão, o sistema configura:
- E-mail: `admin@biblioteca.com`
- Senha: `Biblioteca@2026`

Para apresentação e uso real, altere as credenciais por meio das variáveis `BIBLIOTECA_ADMIN_EMAIL` e `BIBLIOTECA_ADMIN_SENHA`. Não publique credenciais reais.

## Observação
A página inicial servida pelo backend está em `backend/src/main/webapp/index.html`. O `index.html` na raiz é uma página visual independente e não é a entrada da aplicação conectada à API.
