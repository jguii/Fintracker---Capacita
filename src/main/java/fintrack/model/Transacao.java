package fintrack.model;

import fintrack.utils.Formatador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade central do sistema: uma receita ou despesa.
 * <p>
 * Melhorias em relação à versão console:
 * <ul>
 *   <li>{@link BigDecimal} no lugar de {@code double} — evita erros de arredondamento
 *       em valores monetários (ex.: 0.1 + 0.2 != 0.3 com double);</li>
 *   <li>{@link TipoTransacao} no lugar de {@code boolean ehReceita};</li>
 *   <li>{@code id} para permitir persistência em banco de dados;</li>
 *   <li>campo {@code observacao} opcional (editado via TextArea na interface);</li>
 *   <li>invariantes garantidas também nos setters, não só no construtor.</li>
 * </ul>
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

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição não pode estar vazia!");
        }
        this.descricao = descricao.trim();
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero!");
        }
        this.valor = valor.setScale(ESCALA, RoundingMode.HALF_EVEN);
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = Objects.requireNonNull(tipo, "O tipo da transação é obrigatório!");
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data não pode ser nula!");
        }
        this.data = data;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = (observacao == null || observacao.isBlank()) ? null : observacao.trim();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s - %s",
                getRotuloTipo(), descricao, Formatador.formatarValor(valor), Formatador.formatarData(data));
    }
}
