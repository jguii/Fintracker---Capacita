package fintrack.console;

import fintrack.dao.Conexao;
import fintrack.dao.TransacaoDAO;
import fintrack.exceptions.EntradaInvalidaException;
import fintrack.exceptions.PersistenciaException;
import fintrack.model.ResumoFinanceiro;
import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import fintrack.model.TransacaoMensal;
import fintrack.service.TransacaoService;
import fintrack.utils.Colecoes;
import fintrack.utils.Formatador;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Versão console original do FinTrack, agora usando a mesma camada de serviço e o
 * mesmo banco de dados da interface gráfica (os dados são compartilhados).
 */
public class ConsoleApp {

    private final TransacaoService service;
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleApp(TransacaoService service, Scanner scanner, PrintStream out) {
        this.service = service;
        this.scanner = scanner;
        this.out = out;
    }

    public static void main(String[] args) {
        Conexao conexao = Conexao.padrao();
        conexao.inicializarBanco();
        TransacaoService service = new TransacaoService(new TransacaoDAO(conexao));
        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleApp(service, scanner, System.out).executar();
        }
    }

    public void executar() {
        out.println("===== FINTRACK - SEU CONTROLE FINANCEIRO =====");
        boolean executando = true;
        while (executando && scanner.hasNextLine()) {
            exibirMenu();
            String opcao = scanner.nextLine().trim();
            try {
                switch (opcao) {
                    case "1" -> adicionarTransacao();
                    case "2" -> listarTransacoes();
                    case "3" -> mostrarSaldo();
                    case "4" -> removerTransacao();
                    case "5" -> {
                        executando = false;
                        out.println("Saindo do FinTrack. Até logo!");
                    }
                    default -> out.println("Opção inválida! Tente novamente.");
                }
            } catch (PersistenciaException e) {
                out.println("Erro de banco de dados: " + e.getMessage());
            }
        }
    }

    private void exibirMenu() {
        out.println();
        out.println("--- MENU ---");
        out.println("1. Adicionar nova transação");
        out.println("2. Listar transações");
        out.println("3. Mostrar saldo atual");
        out.println("4. Remover transação");
        out.println("5. Sair");
        out.print("Escolha uma opção: ");
    }

    private void adicionarTransacao() {
        try {
            String descricao = perguntar("Descrição da transação: ");
            BigDecimal valor = Formatador.parseValor(perguntar("Valor (R$): "));
            TipoTransacao tipo = confirmar("É receita? (s/n): ") ? TipoTransacao.RECEITA : TipoTransacao.DESPESA;
            LocalDate data = Formatador.parseData(perguntar("Data (dd/MM/yyyy): "));

            Transacao transacao;
            if (confirmar("É transação mensal? (s/n): ")) {
                int dia = Integer.parseInt(perguntar("Dia do mês (1-31): "));
                transacao = new TransacaoMensal(descricao, valor, tipo, data, dia);
            } else {
                transacao = new Transacao(descricao, valor, tipo, data);
            }

            service.adicionar(transacao);
            out.println("Transação adicionada com sucesso!");
        } catch (DateTimeParseException e) {
            out.println("Erro: Formato de data inválido! Use dd/MM/yyyy.");
        } catch (NumberFormatException e) {
            out.println("Erro: Valor ou número inválido!");
        } catch (IllegalArgumentException | EntradaInvalidaException e) {
            out.println("Erro: " + e.getMessage());
        }
    }

    private void listarTransacoes() {
        List<Transacao> transacoes = service.listar();
        if (Colecoes.isVazia(transacoes)) {
            out.println("Nenhuma transação registrada.");
            return;
        }
        out.println();
        out.println("--- LISTA DE TRANSAÇÕES ---");
        Colecoes.imprimirNumerado(transacoes, out);
    }

    private void mostrarSaldo() {
        ResumoFinanceiro resumo = service.gerarResumo();
        out.println();
        out.println("--- SALDO ATUAL ---");
        out.println("Receitas: " + Formatador.formatarValor(resumo.totalReceitas()));
        out.println("Despesas: " + Formatador.formatarValor(resumo.totalDespesas()));
        out.println("Saldo:    " + Formatador.formatarValor(resumo.saldo()));
    }

    private void removerTransacao() {
        List<Transacao> transacoes = service.listar();
        if (Colecoes.isVazia(transacoes)) {
            out.println("Nenhuma transação para remover.");
            return;
        }
        out.println();
        out.println("--- REMOVER TRANSAÇÃO ---");
        Colecoes.imprimirNumerado(transacoes, out);
        try {
            int posicao = Integer.parseInt(perguntar("Número da transação para remover: "));
            Transacao alvo = Colecoes.elementoNaPosicao(transacoes, posicao);
            service.remover(alvo.getId());
            out.println("Transação removida com sucesso!");
        } catch (NumberFormatException e) {
            out.println("Erro: Digite um número válido!");
        } catch (IndexOutOfBoundsException e) {
            out.println("Erro: Índice inválido! Selecione uma transação válida.");
        } catch (EntradaInvalidaException e) {
            out.println("Erro: " + e.getMessage());
        }
    }

    private String perguntar(String rotulo) {
        out.print(rotulo);
        return scanner.nextLine().trim();
    }

    private boolean confirmar(String rotulo) {
        String resposta = perguntar(rotulo).toLowerCase();
        return resposta.equals("s") || resposta.equals("sim");
    }
}
