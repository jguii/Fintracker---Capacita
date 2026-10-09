package fintrack.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransacaoTest {

    @Test
    void criacaoValidaReceita() {
        Transacao t = new Transacao("Salário", new BigDecimal("5000.50"), TipoTransacao.RECEITA, LocalDate.now());
        
        assertEquals("Salário", t.getDescricao());
        assertEquals(new BigDecimal("5000.50"), t.getValor());
        assertEquals(TipoTransacao.RECEITA, t.getTipo());
        assertTrue(t.isReceita());
        assertFalse(t.isDespesa());
        assertEquals(new BigDecimal("5000.50"), t.getValorComSinal());
    }

    @Test
    void criacaoValidaDespesa() {
        Transacao t = new Transacao("Luz", new BigDecimal("150.75"), TipoTransacao.DESPESA, LocalDate.now());
        
        assertTrue(t.isDespesa());
        assertEquals(new BigDecimal("-150.75"), t.getValorComSinal());
    }

    @Test
    void descricaoInvalidaLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Transacao("", new BigDecimal("100"), TipoTransacao.RECEITA, LocalDate.now()));
            
        assertThrows(IllegalArgumentException.class, () -> 
            new Transacao("   ", new BigDecimal("100"), TipoTransacao.RECEITA, LocalDate.now()));
            
        assertThrows(IllegalArgumentException.class, () -> 
            new Transacao(null, new BigDecimal("100"), TipoTransacao.RECEITA, LocalDate.now()));
    }

    @Test
    void valorInvalidoLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Transacao("Teste", BigDecimal.ZERO, TipoTransacao.RECEITA, LocalDate.now()));
            
        assertThrows(IllegalArgumentException.class, () -> 
            new Transacao("Teste", new BigDecimal("-10"), TipoTransacao.RECEITA, LocalDate.now()));
    }

    @Test
    void recorrenciaMensalValidaDia() {
        assertThrows(IllegalArgumentException.class, () -> 
            new TransacaoMensal("Internet", new BigDecimal("100"), TipoTransacao.DESPESA, LocalDate.now(), 0));
            
        assertThrows(IllegalArgumentException.class, () -> 
            new TransacaoMensal("Internet", new BigDecimal("100"), TipoTransacao.DESPESA, LocalDate.now(), 32));
    }
    
    @Test
    void calculoProximaOcorrenciaMensal() {
        TransacaoMensal t = new TransacaoMensal("Aluguel", new BigDecimal("1000"), TipoTransacao.DESPESA, 
                                                LocalDate.of(2024, 1, 15), 31);
                                                
        // Em fevereiro de 2024 (bissexto), o dia 31 vira 29
        assertEquals(LocalDate.of(2024, 2, 29), t.proximaOcorrencia(LocalDate.of(2024, 2, 1)));
        
        // Em abril (30 dias), o dia 31 vira 30
        assertEquals(LocalDate.of(2024, 4, 30), t.proximaOcorrencia(LocalDate.of(2024, 4, 10)));
        
        // Se a referência já passou do dia, vai pro próximo mês
        assertEquals(LocalDate.of(2024, 2, 29), t.proximaOcorrencia(LocalDate.of(2024, 1, 31).plusDays(1)));
    }
}
