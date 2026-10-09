package fintrack.dao;

import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import fintrack.model.TransacaoMensal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransacaoDAOTest {

    private Conexao conexao;
    private TransacaoDAO dao;

    @BeforeEach
    void setUp() {
        // Usa banco em memória (SQLite): rápido e isolado para cada teste
        conexao = Conexao.emMemoria();
        conexao.inicializarBanco();
        dao = new TransacaoDAO(conexao);
    }

    @AfterEach
    void tearDown() {
        conexao.close(); // Descarta o banco em memória
    }

    @Test
    void salvarEBuscarTransacao() {
        Transacao t = new Transacao("Teste", new BigDecimal("150.00"), TipoTransacao.RECEITA, LocalDate.now(), "Obs");
        dao.salvar(t);
        
        assertNotNull(t.getId());
        assertEquals(1, dao.contar());

        Optional<Transacao> doBanco = dao.buscarPorId(t.getId());
        assertTrue(doBanco.isPresent());
        assertEquals("Teste", doBanco.get().getDescricao());
        assertEquals(new BigDecimal("150.00"), doBanco.get().getValor());
        assertEquals(TipoTransacao.RECEITA, doBanco.get().getTipo());
        assertEquals("Obs", doBanco.get().getObservacao());
        assertFalse(doBanco.get().isRecorrente());
    }

    @Test
    void salvarEBuscarTransacaoMensal() {
        TransacaoMensal tm = new TransacaoMensal("Aluguel", new BigDecimal("1200.00"), TipoTransacao.DESPESA, 
                                                 LocalDate.now(), "Fixo", 15);
        dao.salvar(tm);

        Optional<Transacao> doBanco = dao.buscarPorId(tm.getId());
        assertTrue(doBanco.isPresent());
        assertTrue(doBanco.get().isRecorrente());
        assertTrue(doBanco.get() instanceof TransacaoMensal);
        assertEquals(15, ((TransacaoMensal) doBanco.get()).getDiaDoMes());
    }

    @Test
    void salvarEmLoteComTransacao() {
        Transacao t1 = new Transacao("T1", new BigDecimal("10"), TipoTransacao.RECEITA, LocalDate.now());
        Transacao t2 = new Transacao("T2", new BigDecimal("20"), TipoTransacao.RECEITA, LocalDate.now());
        
        dao.salvarTodos(List.of(t1, t2));
        
        assertEquals(2, dao.contar());
        assertNotNull(t1.getId());
        assertNotNull(t2.getId());
    }

    @Test
    void buscarPorPeriodo() {
        dao.salvar(new Transacao("T1", new BigDecimal("10"), TipoTransacao.RECEITA, LocalDate.of(2024, 1, 10)));
        dao.salvar(new Transacao("T2", new BigDecimal("20"), TipoTransacao.RECEITA, LocalDate.of(2024, 1, 15)));
        dao.salvar(new Transacao("T3", new BigDecimal("30"), TipoTransacao.RECEITA, LocalDate.of(2024, 2, 5)));

        List<Transacao> janeiro = dao.listarPorPeriodo(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        assertEquals(2, janeiro.size());
        
        List<Transacao> todos = dao.listarPorPeriodo(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        assertEquals(3, todos.size());
    }
    
    @Test
    void atualizarERemover() {
        Transacao t = dao.salvar(new Transacao("T1", new BigDecimal("10"), TipoTransacao.RECEITA, LocalDate.now()));
        Long id = t.getId();
        
        t.setDescricao("T1_Editado");
        t.setValor(new BigDecimal("99.99"));
        assertTrue(dao.atualizar(t));
        
        Transacao doBanco = dao.buscarPorId(id).get();
        assertEquals("T1_Editado", doBanco.getDescricao());
        assertEquals(new BigDecimal("99.99"), doBanco.getValor());
        
        assertTrue(dao.removerPorId(id));
        assertEquals(0, dao.contar());
    }
}
