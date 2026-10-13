# FinTrack — Controle Financeiro Pessoal (v2)

## Visão Geral

O FinTrack é um sistema de controle financeiro pessoal que permite ao usuário cadastrar receitas e despesas, listar transações, exibir o saldo e filtrar por período. Originalmente construído apenas para execução no console, agora na **versão 2.0** ele evoluiu para incluir uma interface gráfica rica e persistência em banco de dados.

## Novas Tecnologias Utilizadas na v2

A versão 2.0 do projeto consolida conceitos avançados de Java e padrões de projeto:

### 1. JavaFX — Interface Gráfica
* **Telas:** `MainView` (tabela de transações e resumo mensal), `TransacaoFormView` (formulário de inserção e edição) e `RelatorioView` (gráfico e extrato textual).
* **Componentes:** TableView, TableColumn, CheckBox, DatePicker, Spinner, PieChart, TextArea, etc.
* **FXML e Scene Builder:** Telas separadas em `.fxml`, vinculadas ao seu respectivo Controller via `fx:controller`.
* **CSS:** Aparência totalmente personalizada utilizando a folha de estilos `styles.css` para cartões de saldo e formatação de valores e erros.

### 2. Banco de Dados JDBC (SQLite)
* **Persistência:** Implementação de `TransacaoDAO` que persiste e busca dados no arquivo `~/.fintrack/fintrack.db`.
* **SQLite em Memória:** Capacidade de gerar bancos em memória para isolar testes unitários e de integração (`Conexao.emMemoria()`).
* **Segurança e Transações:** Utilização extensiva de `PreparedStatement` (prevenção contra SQL Injection) e blocos transacionais explícitos (`commit()` e `rollback()`) na adição em lote.

### 3. Generics e Curingas (Wildcards)
* **Repositório Genérico:** A interface `Repositorio<T extends Identificavel<ID>, ID>` isola as operações CRUD, implementada por `RepositorioGenerico` (em memória) e adaptada no `TransacaoDAO` (banco).
* **Serviços:** `ServicoGenerico` implementa o padrão *Template Method*, reutilizando a lógica de "validar -> salvar".
* **Curingas (Wildcards):** Demonstração do padrão PECS (Producer Extends, Consumer Super). Uso de `?`, `? extends T` (ex. para ler coleções de subclasses como `TransacaoMensal`) e `? super T` (ex. para `Predicate` e `Comparator`).

### 4. Testes de Software com JUnit 5
* Testes unitários para entidades (`TransacaoTest`, `CalculadoraFinanceiraTest`).
* Testes de integração em banco em memória (`TransacaoDAOTest`) garantindo o funcionamento do DAO de forma independente e isolada.

## Estrutura do Projeto (Maven)

O projeto migrou para a estrutura padrão do Maven (`src/main/java`, `src/main/resources`, `src/test/java`).

* **fintrack.app** — Contém o `FinApp` (ciclo de vida JavaFX) e o `Launcher`.
* **fintrack.console** — A interface console original que agora reaproveita a camada de banco de dados e os serviços genéricos da interface gráfica.
* **fintrack.controller** — Controllers das telas do JavaFX, ligados ao FXML.
* **fintrack.dao** — Acesso a banco com JDBC (SQLite).
* **fintrack.exceptions** — Exceções de persistência e validação.
* **fintrack.model** — Entidades (`Transacao`, `TransacaoMensal`, `TipoTransacao`, `ResumoFinanceiro`).
* **fintrack.repository** — Interface padrão de acesso a dados e implementação genérica em memória.
* **fintrack.service** — Camada que contém as regras de negócio antes de persistir no DAO.
* **fintrack.ui** — Factories, formatadores e alertas para o JavaFX.
* **fintrack.utils** — Utilitários de data, coleções e formatação.

## Como Executar

Se você tiver o Apache Maven e o JDK instalados, pode utilizar os plugins configurados:

**Executar a interface gráfica (JavaFX):**
```bash
mvn clean javafx:run
```

**Executar a versão Console:**
```bash
mvn clean exec:java
```

**Rodar os testes automatizados:**
```bash
mvn clean test
```

**Empacotar o projeto em um JAR (Fat JAR/Uber JAR):**
```bash
mvn clean package
```
Isto gerará um arquivo executável na pasta `target/fintrack.jar`. 
Para executá-lo independentemente:
```bash
java -jar target/fintrack.jar
```
Ou simplesmente dê um duplo clique no arquivo **`iniciar.bat`** (no Windows).

