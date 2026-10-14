# FinTrack — Sistema de Gestão Financeira Pessoal

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-2C3E50?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

O **FinTrack** é uma aplicação desktop para gestão financeira pessoal desenvolvida em **Java 21** e **JavaFX**. Projetado com foco em usabilidade, precisão contábil e arquitetura limpa, o sistema oferece controle completo de receitas, despesas e transações recorrentes, persistência robusta em **MySQL** e relatórios visuais com gráficos.

---

## Recursos do Sistema

- **Gestão de Lançamentos**: Cadastro, edição, listagem e remoção de receitas e despesas com validação imediata de dados.
- **Painel Lateral de Detalhes**: Painel retrátil na tela principal que apresenta metadados completos, valores formatados, badges de identificação e visualização integral do campo de **observações** com rolagem dinâmica.
- **Transações Recorrentes Mensais**: Suporte a lançamentos fixos (salários, aluguel, assinaturas), com dia de competência configurável e destaque visual diferenciado na tabela.
- **Filtros e Busca em Tempo Real**: Consulta instantânea por descrição e filtro de exibição para o mês corrente.
- **Relatório e Extrato com Gráfico**: Modal de fechamento mensal com gráfico interativo proporcional (receitas vs. despesas), indicador de saldo consolidado e extrato textual com função de cópia para a área de transferência.
- **Múltiplas Interfaces**:
  - **Interface Gráfica (Desktop)** via JavaFX, responsiva e com tema visual personalizado.
  - **Interface de Terminal (CLI)** com suporte às mesmas operações e compartilhamento da base de dados.

---

## Arquitetura e Engenharia de Software

- **Padrões de Projeto**:
  - **MVC (Model-View-Controller)**: Separação estrutural estrita entre a camada visual declarativa (`FXML` e `CSS`), controladores reativos de tela (`MainController`, `RelatorioController`, `TransacaoFormController`) e modelos de domínio.
  - **DAO (Data Access Object)**: Desacoplamento da persistência através do contrato `Repositorio`, permitindo trocar ou estender o mecanismo de dados sem impacto nas regras de negócio.
  - **Template Method**: Reutilização do pipeline padronizado de validação e persistência na classe abstrata `ServicoGenerico`.
  - **Generics & Wildcards**: Tipagem estrita com tipos limitados (`<T extends Identificavel<ID>, ID>`) e curingas (`? extends T`, `? super T`), assegurando polimorfismo seguro em operações em lote e predicados de filtro.
- **Precisão Monetária**: Todos os cálculos contábeis utilizam `BigDecimal` com escala fixa de 2 casas decimais e arredondamento bancário (`HALF_EVEN`), eliminando erros de ponto flutuante típicos de `double`/`float`.
- **Segurança e Transações**: Todas as operações SQL são parametrizadas via `PreparedStatement` (blindagem contra SQL Injection) e operações em lote contam com controle transacional explícito (`commit()` e `rollback()`).
- **Suíte de Testes Automatizados**: Cobertura de 32 testes unitários e de integração com JUnit 5. A suíte executa sobre banco H2 em memória em modo de compatibilidade com MySQL (`MODE=MySQL`), garantindo testes 100% isolados, ultrarrápidos e independentes de serviços externos ativos.

---

## Pré-requisitos

- **Java Development Kit (JDK) 21** ou superior instalado.
- **MySQL Server 8.0+** ativo na porta `3306`.
- *(Opcional)* Apache Maven. O projeto já inclui o **Maven Wrapper** (`mvnw` / `mvnw.cmd`).

---

## Instalação e Configuração

### 1. Clonar o repositório
```bash
git clone https://github.com/SEU-USUARIO/fintrack.git
cd fintrack
```

### 2. Criar a base de dados no MySQL
Execute o comando abaixo no MySQL para criar o schema da aplicação:
```sql
CREATE DATABASE fintrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar as credenciais de acesso
Copie o arquivo de exemplo para gerar o seu arquivo local de configuração:

- **Windows (PowerShell):**
  ```powershell
  Copy-Item db.properties.example db.properties
  ```
- **Linux / macOS:**
  ```bash
  cp db.properties.example db.properties
  ```

Edite o arquivo `db.properties` com suas credenciais do MySQL:
```properties
db.url=jdbc:mysql://localhost:3306/fintrack?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8
db.user=root
db.password=sua_senha
```

---

## Executando o Projeto

### Inicialização Rápida no Windows
Dê um duplo clique no arquivo **`iniciar.bat`** na raiz do projeto. Ele compila automaticamente o executável caso seja a primeira execução e abre o FinTrack diretamente.

### Via Linha de Comando (Maven Wrapper)

- **Executar Interface Gráfica (JavaFX):**
  ```bash
  ./mvnw javafx:run
  # Windows:
  .\mvnw.cmd javafx:run
  ```

- **Executar Interface em Linha de Comando (Console):**
  ```bash
  ./mvnw exec:java
  # Windows:
  .\mvnw.cmd exec:java
  ```

- **Executar a Suíte de Testes:**
  ```bash
  ./mvnw test
  # Windows:
  .\mvnw.cmd test
  ```

- **Empacotar em arquivo JAR executável:**
  ```bash
  ./mvnw package -DskipTests
  # Windows:
  .\mvnw.cmd package -DskipTests
  ```
  O arquivo gerado estará localizado em `target/fintrack.jar` e pode ser iniciado com:
  ```bash
  java -jar target/fintrack.jar
  ```

---

## Estrutura de Diretórios

```text
fintrack/
├── .gitignore
├── db.properties.example      # Modelo de configuração de banco de dados
├── iniciar.bat                # Script de execução rápida no Windows
├── mvnw / mvnw.cmd            # Apache Maven Wrapper
├── pom.xml                    # Configuração de dependências e build
├── src/
│   ├── main/
│   │   ├── java/fintrack/
│   │   │   ├── app/           # Launcher e ciclo de vida da aplicação
│   │   │   ├── console/       # Interface de linha de comando (CLI)
│   │   │   ├── controller/    # Controllers das telas JavaFX
│   │   │   ├── dao/           # Conexão e persistência JDBC com MySQL
│   │   │   ├── exceptions/    # Exceções de negócio e persistência
│   │   │   ├── model/         # Entidades de domínio (Transacao, etc.)
│   │   │   ├── repository/    # Abstração de repositórios e coleções
│   │   │   ├── service/       # Regras de negócio e cálculos financeiros
│   │   │   ├── ui/            # Formatação de células, diálogos e gerenciador de telas
│   │   │   └── utils/         # Formatadores monetários e de datas
│   │   └── resources/
│   │       ├── db/            # Script DDL de criação do schema SQL
│   │       └── fintrack/
│   │           ├── css/       # Folhas de estilo da aplicação (styles.css)
│   │           └── view/      # Arquivos de interface FXML
│   └── test/java/fintrack/    # 32 testes automatizados com JUnit 5 e H2
```

---

## Licença

Distribuído sob a licença **MIT**.
