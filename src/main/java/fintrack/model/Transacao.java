package fintrack.model;

import fintrack.utils.Formatador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade central do sistema: representa uma transação financeira (receita ou despesa).
 * <p>
 * Mantém invariantes de domínio, controle monetário preciso via {@link BigDecimal}
 * e suporte a observações adicionais.
 */
public class Transacao implements Identificavel<Long> {

    /** Escala monetária padrão: 2 casas decimais. */
    public static final int ESCALA = 2;

    private Long id;
    private String descricao;
    private BigDecimal valor;
    private TipoTransacao tipo;
    private LocalDate data;
    private String observacao;

    public Transacao(String descricao, BigDecimal valor, TipoTransacao tipo, LocalDate data) {
        this(descricao, valor, tipo, data, null);
    }

    public Transacao(String descricao, BigDecimal valor, TipoTransacao tipo, LocalDate data, String observacao) {
        setDescricao(descricao);
        setValor(valor);
        setTipo(tipo);
        setData(data);
        setObservacao(observacao);
    }

    // ------------------------------------------------------------------ regras

    /** Valor com sinal contábil: positivo para receita, negativo para despesa. */
    public BigDecimal getValorComSinal() {
        return tipo.aplicarSinal(valor);
    }

    public boolean isReceita() {
        return tipo == TipoTransacao.RECEITA;
    }

    public boolean isDespesa() {
        return tipo == TipoTransacao.DESPESA;
    }

    /** Transações simples não se repetem. Sobrescrito em {@link TransacaoMensal}. */
    public boolean isRecorrente() {
        return false;
    }

    /** Rótulo do tipo exibido em telas e relatórios. */
    public String getRotuloTipo() {
        return tipo.getDescricao();
    }

    // -------------------------------------------------------- getters/setters

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public final void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição não pode estar vazia!");
        }
        this.descricao = descricao.trim();
    }

    public BigDecimal getValor() {
        return valor;
    }

    public final void setValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero!");
        }
        this.valor = valor.setScale(ESCALA, RoundingMode.HALF_EVEN);
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public final void setTipo(TipoTransacao tipo) {
        this.tipo = Objects.requireNonNull(tipo, "O tipo da transação é obrigatório!");
    }

    public LocalDate getData() {
        return data;
    }

    public final void setData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data não pode ser nula!");
        }
        this.data = data;
    }

    public String getObservacao() {
        return observacao;
    }

    public final void setObservacao(String observacao) {
        this.observacao = (observacao == null || observacao.isBlank()) ? null : observacao.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transacao transacao = (Transacao) o;
        if (id != null && transacao.id != null) {
            return Objects.equals(id, transacao.id);
        }
        return Objects.equals(descricao, transacao.descricao)
                && Objects.equals(valor, transacao.valor)
                && tipo == transacao.tipo
                && Objects.equals(data, transacao.data);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : Objects.hash(descricao, valor, tipo, data);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s - %s",
                getRotuloTipo(), descricao, Formatador.formatarValor(valor), Formatador.formatarData(data));
    }
}
