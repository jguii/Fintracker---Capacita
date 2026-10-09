package fintrack.service;

import fintrack.model.ResumoFinanceiro;
import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraFinanceiraTest {

    private final Transacao r1 = new Transacao("R1", new BigDecimal("1000.00"), TipoTransacao.RECEITA, LocalDate.now());
    private final Transacao r2 = new Transacao("R2", new BigDecimal("500.00"), TipoTransacao.RECEITA, LocalDate.now());
    private final Transacao d1 = new Transacao("D1", new BigDecimal("300.00"), TipoTransacao.DESPESA, LocalDate.now());
    private final Transacao d2 = new Transacao("D2", new BigDecimal("800.50"), TipoTransacao.DESPESA, LocalDate.now());

    @Test
    void calcularSaldoCorreto() {
        List<Transacao> lista = List.of(r1, d1, r2);
        // 1000 - 300 + 500 = 1200
        assertEquals(new BigDecimal("1200.00"), CalculadoraFinanceira.calcularSaldo(lista));
    }

    @Test
    void saldoNegativo() {
        List<Transacao> lista = List.of(r1, d1, d2);
        // 1000 - 300 - 800.50 = -100.50
        assertEquals(new BigDecimal("-100.50"), CalculadoraFinanceira.calcularSaldo(lista));
    }

    @Test
    void somarPorTipo() {
        List<Transacao> lista = List.of(r1, d1, r2, d2);
        assertEquals(new BigDecimal("1500.00"), CalculadoraFinanceira.somarPorTipo(lista, TipoTransacao.RECEITA));
        assertEquals(new BigDecimal("1100.50"), CalculadoraFinanceira.somarPorTipo(lista, TipoTransacao.DESPESA));
    }

    @Test
    void gerarResumoCompleto() {
        List<Transacao> lista = List.of(r1, d1, r2, d2);
        ResumoFinanceiro resumo = CalculadoraFinanceira.resumir(lista);
        
        assertEquals(new BigDecimal("1500.00"), resumo.totalReceitas());
        assertEquals(new BigDecimal("1100.50"), resumo.totalDespesas());
        assertEquals(new BigDecimal("399.50"), resumo.saldo());
        assertEquals(4, resumo.quantidade());
        assertTrue(resumo.isPositivo());
    }

    @Test
    void resumoVazio() {
        ResumoFinanceiro resumo = CalculadoraFinanceira.resumir(List.of());
        
        assertEquals(new BigDecimal("0.00"), resumo.totalReceitas());
        assertEquals(new BigDecimal("0.00"), resumo.totalDespesas());
        assertEquals(new BigDecimal("0.00"), resumo.saldo());
        assertEquals(0, resumo.quantidade());
        assertTrue(resumo.isPositivo()); // Zero é considerado não-negativo
    }
}
