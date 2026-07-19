package app;

import controller.FinTracker;
import exceptions.EntradaInvalidaException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import model.Transacao;
import model.TransacaoMensal;
import utils.Formatador;

public class Main {
    private static FinTracker finTracker = new FinTracker();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===== FINTRACK - SEU CONTROLE FINANCEIRO =====");
        
        boolean executando = true;
        
        while (executando) {
            exibirMenu();
            int opcao = lerOpcao();
            
            switch (opcao) {
                case 1:
                    adicionarTransacao();
                    break;
                case 2:
                    listarTransacoes();
                    break;
                case 3:
                    mostrarSaldo();
                    break;
                case 4:
                    removerTransacao();
                    break;
                case 5:
                    executando = false;
                    System.out.println("Saindo do FinTrack. Até logo!");
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        }
        
        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Adicionar nova transação");
        System.out.println("2. Listar transações");
        System.out.println("3. Mostrar saldo atual");
        System.out.println("4. Remover transação");
        System.out.println("5. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static int lerOpcao() {
        try {
            int opcao = Integer.parseInt(scanner.nextLine().trim());
            return opcao;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void adicionarTransacao() {
        try {
            System.out.print("Descrição da transação: ");
            String descricao = scanner.nextLine().trim();
            
            System.out.print("Valor (R$): ");
            double valor = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            
            System.out.print("É receita? (s/n): ");
            String tipo = scanner.nextLine().trim().toLowerCase();
            boolean ehReceita = tipo.equals("s") || tipo.equals("sim");
            
            System.out.print("Data (dd/MM/yyyy): ");
            LocalDate data = Formatador.parseData(scanner.nextLine().trim());
            
            System.out.print("É transação mensal? (s/n): ");
            String mensal = scanner.nextLine().trim().toLowerCase();
            
            Transacao transacao;
            if (mensal.equals("s") || mensal.equals("sim")) {
                System.out.print("Dia do mês (1-31): ");
                int diaDoMes = Integer.parseInt(scanner.nextLine().trim());
                transacao = new TransacaoMensal(descricao, valor, ehReceita, data, diaDoMes);
            } else {
                transacao = new Transacao(descricao, valor, ehReceita, data);
            }
            
            finTracker.adicionarTransacao(transacao);
            System.out.println("Transação adicionada com sucesso!");
            
        } catch (DateTimeParseException e) {
            System.out.println("Erro: Formato de data inválido! Use dd/MM/yyyy.");
        } catch (NumberFormatException e) {
            System.out.println("Erro: Valor ou número inválido!");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (EntradaInvalidaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void listarTransacoes() {
        List<Transacao> transacoes = finTracker.listarTransacoes();
        
        if (transacoes.isEmpty()) {
            System.out.println("Nenhuma transação registrada.");
            return;
        }
        
        System.out.println("\n--- LISTA DE TRANSAÇÕES ---");
        for (int i = 0; i < transacoes.size(); i++) {
            System.out.println((i + 1) + ". " + transacoes.get(i));
        }
    }

    private static void mostrarSaldo() {
        double saldo = finTracker.calcularSaldoTotal();
        System.out.println("\n--- SALDO ATUAL ---");
        System.out.println("Saldo: " + Formatador.formatarValor(saldo));
    }

    private static void removerTransacao() {
        List<Transacao> transacoes = finTracker.listarTransacoes();
        
        if (transacoes.isEmpty()) {
            System.out.println("Nenhuma transação para remover.");
            return;
        }
        
        System.out.println("\n--- REMOVER TRANSAÇÃO ---");
        for (int i = 0; i < transacoes.size(); i++) {
            System.out.println((i + 1) + ". " + transacoes.get(i));
        }
        
        try {
            System.out.print("Número da transação para remover: ");
            int indice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            
            finTracker.removerTransacao(indice);
            System.out.println("Transação removida com sucesso!");
            
        } catch (NumberFormatException e) {
            System.out.println("Erro: Digite um número válido!");
        } catch (EntradaInvalidaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
