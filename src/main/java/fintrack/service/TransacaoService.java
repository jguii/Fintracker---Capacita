package fintrack.service;

import fintrack.dao.TransacaoDAO;
import fintrack.exceptions.EntradaInvalidaException;
import fintrack.model.ResumoFinanceiro;
import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import fintrack.repository.Repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Camada de serviços e regras de negócio de transações financeiras.
 *
 * Recebe qualquer {@code Repositorio<Transacao, Long>} por injeção de dependência:
 * {@link fintrack.dao.TransacaoDAO} em produção, {@link fintrack.repository.RepositorioGenerico}
 * nos testes ou em modo em memória.
 */
public class TransacaoService extends ServicoGenerico<Transacao, Long> {

    static final int TAMANHO_MAXIMO_DESCRICAO = 100;
    static final BigDecimal VALOR_MAXIMO = new BigDecimal("999999999.99");
    static final LocalDate DATA_MINIMA = LocalDate.of(1900, 1, 1);

    public TransacaoService(Repositorio<Transacao, Long> repositorio) {
        super(repositorio);
    }

    @Override
    protected String nomeEntidade() {
        return "Transação";
    }

    /**
     * Invariantes básicas (descrição, valor > 0, data) já são garantidas pela própria
     * {@link Transacao}. Aqui ficam as regras de negócio adicionais.
     */
    @Override
    protected void validar(Transacao t) throws EntradaInvalidaException {
        if (t.getDescricao().length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new EntradaInvalidaException(
                    "A descrição deve ter no máximo " + TAMANHO_MAXIMO_DESCRICAO + " caracteres!");
        }
        if (t.getValor().compareTo(VALOR_MAXIMO) > 0) {
            throw new EntradaInvalidaException("O valor excede o limite permitido!");
        }
        if (t.getData().isBefore(DATA_MINIMA)) {
            throw new EntradaInvalidaException("Data inválida: anterior a 1900!");
        }
    }

    // ----------------------------------------------------------- consultas

    public List<Transacao> listarPorTipo(TipoTransacao tipo) {
        return listar(t -> t.getTipo() == tipo);
    }

    public List<Transacao> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws EntradaInvalidaException {
        validarPeriodo(inicio, fim);
        if (repositorio instanceof TransacaoDAO dao) {
            return dao.listarPorPeriodo(inicio, fim);
        }
        return listar(t -> !t.getData().isBefore(inicio) && !t.getData().isAfter(fim));
    }

    // ------------------------------------------------------------- cálculos

    public BigDecimal calcularSaldo() {
        return CalculadoraFinanceira.calcularSaldo(listar());
    }

    public BigDecimal totalReceitas() {
        return CalculadoraFinanceira.somarPorTipo(listar(), TipoTransacao.RECEITA);
    }

    public BigDecimal totalDespesas() {
        return CalculadoraFinanceira.somarPorTipo(listar(), TipoTransacao.DESPESA);
    }

    public ResumoFinanceiro gerarResumo() {
        return CalculadoraFinanceira.resumir(listar());
    }

    public ResumoFinanceiro gerarResumo(LocalDate inicio, LocalDate fim) throws EntradaInvalidaException {
        return CalculadoraFinanceira.resumir(listarPorPeriodo(inicio, fim));
    }

    private static void validarPeriodo(LocalDate inicio, LocalDate fim) throws EntradaInvalidaException {
        if (inicio == null || fim == null) {
            throw new EntradaInvalidaException("Informe a data inicial e a data final!");
        }
        if (inicio.isAfter(fim)) {
            throw new EntradaInvalidaException("A data inicial não pode ser posterior à data final!");
        }
    }
}
