package fintrack.service;

import fintrack.dao.Conexao;
import fintrack.dao.TransacaoDAO;
import fintrack.exceptions.EntradaInvalidaException;
import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import fintrack.repository.RepositorioGenerico;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransacaoServiceTest {

    private Conexao conexao;
    private TransacaoDAO dao;
    private TransacaoService serviceComDao;
    private TransacaoService serviceComMemoria;

    @BeforeEach
    void setUp() {
        conexao = Conexao.emMemoria();
        conexao.inicializarBanco();
        dao = new TransacaoDAO(conexao);
        serviceComDao = new TransacaoService(dao);
        serviceComMemoria = new TransacaoService(RepositorioGenerico.comIdLong());
    }

    @AfterEach
    void tearDown() {
        conexao.close();
    }

    @Test
    void descricaoMuitoLongaLancaExcecao() {
        String descricaoLonga = "A".repeat(TransacaoService.TAMANHO_MAXIMO_DESCRICAO + 1);
        Transacao t = new Transacao(descricaoLonga, new BigDecimal("100"), TipoTransacao.RECEITA, LocalDate.now());

        assertThrows(EntradaInvalidaException.class, () -> serviceComDao.adicionar(t));
    }

    @Test
    void valorAcimaDoLimiteLancaExcecao() {
        BigDecimal valorGigante = TransacaoService.VALOR_MAXIMO.add(BigDecimal.ONE);
        Transacao t = new Transacao("Teste", valorGigante, TipoTransacao.RECEITA, LocalDate.now());

        assertThrows(EntradaInvalidaException.class, () -> serviceComDao.adicionar(t));
    }

    @Test
    void dataAnterior1900LancaExcecao() {
        LocalDate dataAntiga = LocalDate.of(1899, 12, 31);
        Transacao t = new Transacao("Teste", new BigDecimal("100"), TipoTransacao.RECEITA, dataAntiga);

        assertThrows(EntradaInvalidaException.class, () -> serviceComDao.adicionar(t));
    }

    @Test
    void periodoInvalidoLancaExcecao() {
        LocalDate inicio = LocalDate.of(2026, 10, 10);
        LocalDate fim = LocalDate.of(2026, 10, 5);

        assertThrows(EntradaInvalidaException.class, () -> serviceComDao.listarPorPeriodo(inicio, fim));
        assertThrows(EntradaInvalidaException.class, () -> serviceComDao.listarPorPeriodo(null, fim));
    }

    @Test
    void listarPorPeriodoComDAOUsaFiltroCorreto() throws EntradaInvalidaException {
        serviceComDao.adicionar(new Transacao("Outubro 1", new BigDecimal("100"), TipoTransacao.RECEITA, LocalDate.of(2026, 10, 5)));
        serviceComDao.adicionar(new Transacao("Outubro 2", new BigDecimal("200"), TipoTransacao.DESPESA, LocalDate.of(2026, 10, 20)));
        serviceComDao.adicionar(new Transacao("Novembro", new BigDecimal("300"), TipoTransacao.RECEITA, LocalDate.of(2026, 11, 1)));

        List<Transacao> outubro = serviceComDao.listarPorPeriodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(2, outubro.size());
    }

    @Test
    void listarPorPeriodoComMemoriaFuncionaFallback() throws EntradaInvalidaException {
        serviceComMemoria.adicionar(new Transacao("Outubro 1", new BigDecimal("100"), TipoTransacao.RECEITA, LocalDate.of(2026, 10, 5)));
        serviceComMemoria.adicionar(new Transacao("Novembro", new BigDecimal("300"), TipoTransacao.RECEITA, LocalDate.of(2026, 11, 1)));

        List<Transacao> outubro = serviceComMemoria.listarPorPeriodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(1, outubro.size());
    }
}
