package fintrack.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatadorTest {

    @Test
    void parseValorPadraoBrasileiro() {
        assertEquals(new BigDecimal("1234.56"), Formatador.parseValor("1.234,56"));
        assertEquals(new BigDecimal("1234.56"), Formatador.parseValor("1234,56"));
        assertEquals(new BigDecimal("1234.56"), Formatador.parseValor("1234.56"));
        assertEquals(new BigDecimal("1234.56"), Formatador.parseValor("R$ 1.234,56"));
    }

    @Test
    void parseValorInvalidoLancaExcecao() {
        assertThrows(NumberFormatException.class, () -> Formatador.parseValor(""));
        assertThrows(NumberFormatException.class, () -> Formatador.parseValor("abc"));
        assertThrows(NumberFormatException.class, () -> Formatador.parseValor(null));
    }

    @Test
    void formatacaoDataEMesAno() {
        LocalDate data = LocalDate.of(2026, 10, 15);
        assertEquals("15/10/2026", Formatador.formatarData(data));
        assertEquals("", Formatador.formatarData(null));

        YearMonth mes = YearMonth.of(2026, 10);
        assertEquals("Outubro/2026", Formatador.formatarMesAno(mes));
    }
}
