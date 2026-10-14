package fintrack.service;

import fintrack.model.ResumoFinanceiro;
import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Cálculos financeiros puros (sem estado, sem I/O) — fáceis de testar.
 *
 * Todos os métodos recebem {@code Collection<? extends Transacao>}: assim aceitam
 * {@code List<Transacao>}, {@code List<TransacaoMensal>}, {@code Set<...>} etc.
 */
public final class CalculadoraFinanceira {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(Transacao.ESCALA, RoundingMode.HALF_EVEN);

    private CalculadoraFinanceira() {
        // classe utilitária
    }

    /** Saldo = receitas - despesas. */
    public static BigDecimal calcularSaldo(Collection<? extends Transacao> transacoes) {
        BigDecimal saldo = ZERO;
        for (Transacao t : transacoes) {
            saldo = saldo.add(t.getValorComSinal());
        }
        return saldo;
    }

    /** Soma (positiva) das transações do tipo informado. */
    public static BigDecimal somarPorTipo(Collection<? extends Transacao> transacoes, TipoTransacao tipo) {
        BigDecimal total = ZERO;
        for (Transacao t : transacoes) {
            if (t.getTipo() == tipo) {
                total = total.add(t.getValor());
            }
        }
        return total;
    }

    public static ResumoFinanceiro resumir(Collection<? extends Transacao> transacoes) {
        BigDecimal receitas = somarPorTipo(transacoes, TipoTransacao.RECEITA);
        BigDecimal despesas = somarPorTipo(transacoes, TipoTransacao.DESPESA);
        return new ResumoFinanceiro(receitas, despesas, receitas.subtract(despesas), transacoes.size());
    }

    /**
     * Agrupa as transações por mês, em ordem cronológica. Dentro de cada mês, ordena por data
     * e depois por descrição.
     */
    public static SortedMap<YearMonth, List<Transacao>> agruparPorMes(Collection<? extends Transacao> transacoes) {
        return transacoes.stream()
                .map(t -> (Transacao) t)
                .sorted(Comparator.comparing(Transacao::getData)
                        .thenComparing(Transacao::getDescricao, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getData()), TreeMap::new, Collectors.toList()));
    }
}
