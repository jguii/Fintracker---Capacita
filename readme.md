# FinTrack — Documentação do Projeto

## Visão Geral

O FinTrack é um sistema de controle financeiro pessoal executado pelo console. Ele permite ao usuário cadastrar receitas e despesas, listar transações registradas, visualizar o saldo atual e remover transações. O projeto é desenvolvido em Java puro, sem frameworks externos, e está organizado em pacotes que separam responsabilidades de forma clara.

## Estrutura do Projeto

O projeto segue a seguinte organização de pastas e pacotes:

- **app** — Ponto de entrada da aplicação e interação com o usuário via menu de console.
- **controller** — Camada de controle que contém as regras de negócio e opera sobre a lista de transações.
- **model** — Modelos de dados que representam transações financeiras.
- **exceptions** — Exceções personalizadas para tratamento de erros de entrada.
- **utils** — Utilitários para formatação de datas, valores e tipos.

Os arquivos que compõem o projeto são:

- `app/Main.java`
- `controller/FinTracker.java`
- `model/Transacao.java`
- `model/TransacaoMensal.java`
- `exceptions/EntradaInvalidaException.java`
- `utils/Formatador.java`

## Classes

### Transacao

**Arquivo:** `model/Transacao.java`

Classe base que representa uma transação financeira. É a entidade central do sistema.

**Campos:**

- `descricao` (String) — Descrição da transação.
- `valor` (double) — Valor monetário da transação.
- `ehReceita` (boolean) — Indica se a transação é uma receita (true) ou despesa (false).
- `data` (LocalDate) — Data em que a transação ocorreu.

**Construtor:**

Recebe os quatro campos como parâmetros e inicializa o objeto.

**Métodos públicos:**

- `getDescricao()` — Retorna a descrição da transação.
- `setDescricao(String)` — Altera a descrição da transação.
- `getValor()` — Retorna o valor da transação.
- `setValor(double)` — Altera o valor da transação.
- `isEhReceita()` — Retorna true se a transação for receita, false se for despesa.
- `setEhReceita(boolean)` — Altera o tipo da transação.
- `getData()` — Retorna a data da transação.
- `setData(LocalDate)` — Altera a data da transação.
- `toString()` — Retorna a representação textual no formato: `[Tipo] Descrição - R$ Valor - Data`.

### TransacaoMensal

**Arquivo:** `model/TransacaoMensal.java`

Subclasse de `Transacao` que representa uma transação recorrente mensal, como um salário fixo ou aluguel.

**Campos:**

- `diaDoMes` (int) — Dia do mês em que a transação se repete (1 a 31).

**Construtor:**

Recebe descrição, valor, tipo (receita/despesa), data e o dia do mês. Chama o construtor da classe pai (`Transacao`) e define o dia do mês.

**Métodos públicos:**

- `getDiaDoMes()` — Retorna o dia do mês da recorrência.
- `setDiaDoMes(int)` — Altera o dia do mês da recorrência.
- `toString()` — Retorna a representação textual no formato: `[Tipo Mensal] Descrição - R$ Valor - Dia X do mês`. Diferencia-se da classe pai ao indicar que é uma transação mensal.

### FinTracker

**Arquivo:** `controller/FinTracker.java`

Classe controladora que gerencia a lista de transações. É responsável por adicionar, listar, remover transações e calcular o saldo total.

**Campos:**

- `transacoes` (List of Transacao) — Lista interna que armazena todas as transações registradas.

**Construtor:**

Inicializa a lista de transações como uma `ArrayList` vazia.

**Métodos públicos:**

- `adicionarTransacao(Transacao)` — Valida e adiciona uma transação à lista. Lança `EntradaInvalidaException` se a transação for nula, se o valor for menor ou igual a zero, ou se a descrição estiver vazia.
- `listarTransacoes()` — Retorna uma cópia da lista de transações (protege a lista interna).
- `removerTransacao(int)` — Remove uma transação pelo índice informado. Lança `EntradaInvalidaException` se o índice estiver fora do intervalo válido.
- `calcularSaldoTotal()` — Percorre todas as transações e soma o valor das receitas e subtrai o valor das despesas. Retorna o saldo resultante.
- `getTotalTransacoes()` — Retorna a quantidade total de transações registradas.

### Formatador

**Arquivo:** `utils/Formatador.java`

Classe utilitária com métodos estáticos para formatação e interpretação de datas e valores monetários.

**Constantes:**

- `FORMATO_DATA` (DateTimeFormatter) — Formato de data utilizado: `dd/MM/yyyy`.

**Métodos públicos (estáticos):**

- `formatarValor(double)` — Recebe um valor numérico e retorna o texto formatado como moeda brasileira (R$).
- `formatarData(LocalDate)` — Recebe uma data e retorna o texto no formato `dd/MM/yyyy`.
- `parseData(String)` — Recebe um texto no formato `dd/MM/yyyy` e retorna um objeto `LocalDate`. Lança `DateTimeParseException` caso o formato esteja incorreto.
- `formatarTipo(boolean)` — Recebe um boolean e retorna o texto "Receita" ou "Despesa".

### EntradaInvalidaException

**Arquivo:** `exceptions/EntradaInvalidaException.java`

Exceção personalizada que estende `Exception`. É utilizada para sinalizar erros de validação de entradas, como valores inválidos, descrições vazias ou índices fora do intervalo.

**Construtor:**

Recebe uma mensagem de erro (String) e a repassa ao construtor da classe pai.

### Main

**Arquivo:** `app/Main.java`

Classe principal que contém o método `main` e o menu de interação com o usuário. É o ponto de entrada da aplicação.

**Campos estáticos:**

- `finTracker` (FinTracker) — Instância do controlador que gerencia as transações.
- `scanner` (Scanner) — Leitor de entradas do usuário via console.

**Fluxo de execução:**

Exibe um menu com cinco opções e repete até que o usuário escolha sair. As opções são:

1. **Adicionar nova transação** — Solicita descrição, valor, tipo (receita ou despesa), data e se é uma transação mensal. Se for mensal, solicita também o dia do mês. Valida todas as entradas e trata erros de formatação.
2. **Listar transações** — Exibe todas as transações registradas numeradas, ou informa que nenhuma foi registrada.
3. **Mostrar saldo atual** — Calcula e exibe o saldo total (receitas menos despesas).
4. **Remover transação** — Lista as transações numeradas e solicita o número da transação a ser removida.
5. **Sair** — Encerra o programa.

**Tratamento de erros:**

- `DateTimeParseException` — Data com formato inválido.
- `NumberFormatException` — Valor numérico ou opção de menu inválida.
- `EntradaInvalidaException` — Validações de negócio (transação nula, valor zero ou negativo, descrição vazia, índice inválido).

## Fluxo de Execução

1. O programa inicia e exibe o menu principal.
2. O usuário digita a opção desejada.
3. A opção é processada no bloco `switch`.
4. Para opções de entrada de dados, o programa solicita cada campo e valida conforme as regras de negócio.
5. Se ocorrer algum erro de validação ou formatação, uma mensagem amigável é exibida e o menu reaparece.
6. O loop continua até que o usuário selecione a opção "Sair".

## Compilação e Execução

O projeto deve ser compilado e executado no terminal (PowerShell) do Windows. É necessário ter o **JDK** instalado e configurado no PATH.

**Diretório inicial:** a raiz do projeto (`Fintrack`).

**Compilação:**

```bash
javac -d out -sourcepath . app/Main.java controller/FinTracker.java model/Transacao.java model/TransacaoMensal.java utils/Formatador.java exceptions/EntradaInvalidaException.java
```

**Execução:**

```bash
java -cp out app.Main
```

**Compilar e executar em sequência (um único comando):**

```bash
javac -d out -sourcepath . app/Main.java controller/FinTracker.java model/Transacao.java model/TransacaoMensal.java utils/Formatador.java exceptions/EntradaInvalidaException.java; java -cp out app.Main
```

## Encapsulamento e Convenções

- Todos os campos das classes de modelo são `private`, acessados exclusivamente por getters e setters.
- O getter do campo booleano `ehReceita` segue a convenção JavaBeans para booleanos: `isEhReceita()`.
- A lista de transações no `FinTracker` é protegida: o método `listarTransacoes()` retorna uma cópia, evitando modificações externas à lista interna.
- As exceções personalizadas são usadas para separar erros de validação de negócio de erros de formatação.
