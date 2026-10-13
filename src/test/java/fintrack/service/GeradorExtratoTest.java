package fintrack.service;

import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import fintrack.model.TransacaoMensal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorExtratoTest {

    @Test
    void abreviarDescricao() {
        assertEquals("Curto", GeradorExtrato.abreviar("Curto", 10));
        assertEquals("123456789…", GeradorExtrato.abreviar("1234567890ABC", 10));
        assertEquals("", GeradorExtrato.abreviar(null, 10));
    }

    @Test
    void gerarExtratoVazio() {
        String texto = GeradorExtrato.gerarTexto(List.of(), "Outubro/2026");
        assertTrue(texto.contains("EXTRATO - Outubro/2026"));
        assertTrue(texto.contains("Nenhuma transação no período."));
        assertTrue(texto.contains("SALDO DO PERÍODO:"));
    }

    @Test
    void gerarExtratoComTransacoesERecorrencia() {
        Transacao t1 = new Transacao("Salário", new BigDecimal("3000.00"), TipoTransacao.RECEITA, LocalDate.of(2026, 10, 5));
        TransacaoMensal t2 = new TransacaoMensal("Aluguel", new BigDecimal("1200.00"), TipoTransacao.DESPESA, LocalDate.of(2026, 10, 10), 10);

        String texto = GeradorExtrato.gerarTexto(List.of(t1, t2), "01/10/2026 a 31/10/2026");
        assertTrue(texto.contains("Salário"));
        assertTrue(texto.contains("Aluguel"));
        assertTrue(texto.contains("(Mensal)"));
        assertTrue(texto.contains("SALDO DO PERÍODO:"));
        assertTrue(texto.contains("R$ 1.800,00"));
    }
}
