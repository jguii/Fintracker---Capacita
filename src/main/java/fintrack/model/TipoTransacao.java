package fintrack.model;

import java.math.BigDecimal;

/**
 * Tipo de uma transação financeira.
 * <p>
 * Substitui o antigo campo {@code boolean ehReceita}: um enum é autoexplicativo,
 * extensível e pode ser persistido de forma legível no banco ("RECEITA"/"DESPESA").
 */
public enum TipoTransacao {

    RECEITA("Receita"),
    DESPESA("Despesa");

    private final String descricao;

    TipoTransacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Aplica o sinal contábil ao valor: receitas somam, despesas subtraem.
     */
    public BigDecimal aplicarSinal(BigDecimal valor) {
        return this == RECEITA ? valor : valor.negate();
    }

    @Override
    public String toString() {
        return descricao;
    }
}
