# Biblioteca Virtual

Estrutura inicial para o projeto acadêmico Biblioteca Virtual.

## Tecnologias
- Java 17
- HTML, CSS e JavaScript puro
- H2 Database
- JDBC
- Gson
- BCrypt
- Maven

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
