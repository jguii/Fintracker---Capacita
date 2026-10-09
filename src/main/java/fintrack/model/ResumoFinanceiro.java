package fintrack.model;

import java.math.BigDecimal;

/**
 * Resultado imutável de um relatório financeiro (tela de Relatório/Saldo).
 *
 * @param totalReceitas soma das receitas
 * @param totalDespesas soma das despesas (valor positivo)
 * @param saldo         receitas - despesas
 * @param quantidade    número de transações consideradas
 */
public record ResumoFinanceiro(BigDecimal totalReceitas,
                               BigDecimal totalDespesas,
                               BigDecimal saldo,
                               int quantidade) {

    public static ResumoFinanceiro vazio() {
        BigDecimal zero = BigDecimal.ZERO.setScale(Transacao.ESCALA);
        return new ResumoFinanceiro(zero, zero, zero, 0);
    }

    public boolean isPositivo() {
        return saldo.signum() >= 0;
    }
}
