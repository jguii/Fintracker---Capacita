package fintrack.service;

import fintrack.model.ResumoFinanceiro;
import fintrack.model.Transacao;
import fintrack.utils.Formatador;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

/**
 * Gera o extrato em <b>texto puro</b> (usado pelo botão "Copiar extrato").
 * <p>
 * Colunas de largura fixa: data | descrição | valor | (Mensal). Descrições longas são
 * abreviadas com "…" em vez de cortadas no meio, e a marcação de recorrência fica numa
 * coluna própria após o valor, mantendo os valores alinhados.
 */
public final class GeradorExtrato {

    static final int LARGURA_DESCRICAO = 34;
    private static final int LARGURA_VALOR = 15;
    /** data (10) + 2 espaços + descrição: posição onde começa a coluna de valor. */
    private static final int LARGURA_ROTULO = 10 + 2 + LARGURA_DESCRICAO;
    private static final String LINHA = "-".repeat(LARGURA_ROTULO + 1 + LARGURA_VALOR + 10);

    private GeradorExtrato() {
        // classe utilitária
    }

    public static String gerarTexto(Collection<? extends Transacao> transacoes, String periodo) {
        ResumoFinanceiro resumo = CalculadoraFinanceira.resumir(transacoes);
        SortedMap<YearMonth, List<Transacao>> porMes = CalculadoraFinanceira.agruparPorMes(transacoes);
        boolean agrupar = porMes.size() > 1;

        StringBuilder sb = new StringBuilder();
        sb.append("EXTRATO - ").append(periodo).append('\n').append(LINHA).append('\n');
        if (transacoes.isEmpty()) {
            sb.append("Nenhuma transação no período.\n");
        }

        porMes.forEach((mes, doMes) -> {
            if (agrupar) {
                sb.append('\n').append(Formatador.formatarMesAno(mes).toUpperCase(Formatador.PT_BR)).append('\n');
            }
            for (Transacao t : doMes) {
                sb.append(formatarLinha(t)).append('\n');
            }
            if (agrupar) {
                sb.append(linhaTotal("Subtotal do mês:", CalculadoraFinanceira.calcularSaldo(doMes).toString(),
                        Formatador.formatarValor(CalculadoraFinanceira.calcularSaldo(doMes)), true));
            }
        });

        sb.append(LINHA).append('\n')
                .append(linhaTotal("Total de receitas:", null, Formatador.formatarValor(resumo.totalReceitas()), false))
                .append(linhaTotal("Total de despesas:", null, Formatador.formatarValor(resumo.totalDespesas()), false))
                .append(linhaTotal("SALDO DO PERÍODO:", null, Formatador.formatarValor(resumo.saldo()), false));
        return sb.toString();
    }

    /** Abrevia com "…" quando o texto excede {@code max} caracteres. */
    public static String abreviar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }

    private static String formatarLinha(Transacao t) {
        String linha = String.format("%-10s  %-" + LARGURA_DESCRICAO + "s %" + LARGURA_VALOR + "s  %s",
                Formatador.formatarData(t.getData()),
                abreviar(t.getDescricao(), LARGURA_DESCRICAO),
                Formatador.formatarValor(t.getValorComSinal()),
                t.isRecorrente() ? "(Mensal)" : "");
        return linha.stripTrailing();
    }

    private static String linhaTotal(String rotulo, String ignorado, String valor, boolean alinharDireita) {
        String formato = alinharDireita
                ? "%" + LARGURA_ROTULO + "s %" + LARGURA_VALOR + "s%n"
                : "%-" + LARGURA_ROTULO + "s %" + LARGURA_VALOR + "s%n";
        return String.format(formato, rotulo, valor);
    }
}
