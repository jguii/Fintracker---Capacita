package fintrack.model;

import fintrack.utils.Formatador;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transação recorrente mensal (ex.: salário, aluguel, assinatura).
 * <p>
 * Na interface gráfica é criada quando o CheckBox "Recorrente mensal" está marcado.
 * No banco, é identificada pela coluna {@code dia_recorrencia} preenchida.
 */
public class TransacaoMensal extends Transacao {

    private int diaDoMes;

    public TransacaoMensal(String descricao, BigDecimal valor, TipoTransacao tipo, LocalDate data, int diaDoMes) {
        this(descricao, valor, tipo, data, null, diaDoMes);
    }

    public TransacaoMensal(String descricao, BigDecimal valor, TipoTransacao tipo, LocalDate data,
                           String observacao, int diaDoMes) {
        super(descricao, valor, tipo, data, observacao);
        setDiaDoMes(diaDoMes);
    }

    public int getDiaDoMes() {
        return diaDoMes;
    }

    public final void setDiaDoMes(int diaDoMes) {
        if (diaDoMes < 1 || diaDoMes > 31) {
            throw new IllegalArgumentException("O dia do mês deve estar entre 1 e 31!");
        }
        this.diaDoMes = diaDoMes;
    }

    /**
     * Calcula a próxima data em que a transação ocorre a partir de {@code referencia} (inclusive).
     * Meses mais curtos são tratados: dia 31 em fevereiro vira o último dia do mês.
     */
    public LocalDate proximaOcorrencia(LocalDate referencia) {
        LocalDate candidata = ajustarDia(referencia);
        return candidata.isBefore(referencia) ? ajustarDia(referencia.plusMonths(1)) : candidata;
    }

    private LocalDate ajustarDia(LocalDate mes) {
        return mes.withDayOfMonth(Math.min(diaDoMes, mes.lengthOfMonth()));
    }

    @Override
    public boolean isRecorrente() {
        return true;
    }

    @Override
    public String getRotuloTipo() {
        return super.getRotuloTipo() + " (mensal)";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s - %s - Dia %d do mês",
                getRotuloTipo(), getDescricao(), Formatador.formatarValor(getValor()),
                Formatador.formatarData(getData()), diaDoMes);
    }
}
