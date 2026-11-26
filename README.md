# Biblioteca Virtual

<<<<<<< HEAD
Estrutura inicial para o projeto acadêmico Biblioteca Virtual.
=======
Projeto acadêmico de biblioteca digital com frontend em HTML, CSS e JavaScript, backend Java, JDBC e banco H2.
>>>>>>> master

## Tecnologias
- Java 17
- HTML, CSS e JavaScript puro
- H2 Database
- JDBC
- Gson
- BCrypt
- Maven

<<<<<<< HEAD
## Estrutura
- `frontend/`: páginas, estilos, scripts e imagens.
- `backend/`: aplicação Java e dependências Maven.
- `database/`: script SQL inicial.

## Como executar o backend
1. Instale o JDK 17 ou superior e o Maven.
2. Abra um terminal na pasta `backend`.
3. Execute `mvn clean compile`.
4. Execute `mvn exec:java`.
5. Acesse `http://localhost:8080/api/status`.

O endpoint de status é apenas um teste inicial. Os controllers e a integração completa ainda precisam ser implementados.

## Banco H2
A aplicação está configurada para usar banco H2 em arquivo em `database/biblioteca`. A pasta precisa existir antes da primeira conexão. O script `database/biblioteca.sql` contém a estrutura inicial.

## Próximas tarefas
- Implementar os handlers HTTP dos controllers.
- Inicializar as tabelas automaticamente ou executar o SQL no H2.
- Implementar sessão/autenticação e autorização de administrador.
- Conectar os scripts do frontend à API.
- Testar os fluxos de cadastro, login, catálogo, CRUD e favoritos.

## Segurança
Não faça commit de senhas, tokens ou arquivos de banco com dados pessoais. As senhas dos usuários devem ser armazenadas como hash BCrypt. O controle de acesso administrativo deve ser validado no backend.
=======
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
>>>>>>> master
