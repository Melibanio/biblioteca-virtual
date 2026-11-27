# Biblioteca Virtual

Projeto acadêmico de biblioteca digital com frontend em HTML, CSS e JavaScript, backend Java, JDBC e banco H2.

## Como executar (Windows, sem Maven)
1. Instale o JDK 17 ou superior.
2. Dê dois cliques em `rodar.bat` (ele baixa as bibliotecas na primeira vez, compila e inicia).
3. Acesse `http://localhost:8080`.

No VS Code, também é possível executar a classe `br.com.biblioteca.Main` (botão *Run*), desde que as bibliotecas da pasta `lib/` estejam no classpath.

## Como executar (com Maven)
```
mvn -f backend/pom.xml clean compile
mvn -f backend/pom.xml exec:java
```

## Administrador
- E-mail: `admin@biblioteca.com`
- Senha: `Biblioteca@2026`

Entre em `http://localhost:8080/login.html` e abra **Administração** no menu (ou `http://localhost:8080/admin.html`).
Para trocar as credenciais use as variáveis `BIBLIOTECA_ADMIN_EMAIL` e `BIBLIOTECA_ADMIN_SENHA`.

## Banco de dados
H2 em arquivo (`database/biblioteca`), criado automaticamente a partir de `backend/src/main/resources/schema.sql`.
