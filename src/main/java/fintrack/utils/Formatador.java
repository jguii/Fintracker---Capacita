package fintrack.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Utilitários de formatação/interpretação no padrão brasileiro.
 */
public final class Formatador {

    public static final Locale PT_BR = Locale.of("pt", "BR");

    /** {@code uuuu} + STRICT rejeita datas inexistentes como 31/02/2025. */
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATO_MES_ANO = DateTimeFormatter.ofPattern("MMMM/uuuu", PT_BR);

    private Formatador() {
        // classe utilitária
    }

    /** Ex.: 1234.5 -> "R$ 1.234,50". */
    public static String formatarValor(BigDecimal valor) {
        // NumberFormat não é thread-safe: cria uma instância por chamada.
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor)
                .replace('\u00A0', ' '); // espaço "não separável" -> espaço comum
    }

    public static String formatarData(LocalDate data) {
        return data == null ? "" : data.format(FORMATO_DATA);
    }

    /** Ex.: 2026-10 -> "Outubro/2026". */
    public static String formatarMesAno(YearMonth mes) {
        String texto = mes.format(FORMATO_MES_ANO);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public static LocalDate parseData(String texto) throws DateTimeParseException {
        return LocalDate.parse(texto.trim(), FORMATO_DATA);
    }

    /**
     * Interpreta valores digitados pelo usuário. Aceita "1234.56", "1234,56",
     * "1.234,56" e "R$ 1.234,56".
     *
     * @throws NumberFormatException se o texto não for um número válido
     */
    public static BigDecimal parseValor(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new NumberFormatException("Valor não informado");
        }
        String limpo = texto.replace("R$", "").replaceAll("\\s", "");
        if (limpo.contains(",")) {
            // padrão brasileiro: ponto = milhar, vírgula = decimal
            limpo = limpo.replace(".", "").replace(',', '.');
        }
        return new BigDecimal(limpo);
    }
}
