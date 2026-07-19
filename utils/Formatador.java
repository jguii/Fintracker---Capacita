package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Formatador {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String formatarValor(double valor) {
        return String.format("R$ %.2f", valor);
    }

    public static String formatarData(LocalDate data) {
        return data.format(FORMATO_DATA);
    }

    public static LocalDate parseData(String dataStr) throws DateTimeParseException {
        return LocalDate.parse(dataStr, FORMATO_DATA);
    }

    public static String formatarTipo(boolean ehReceita) {
        return ehReceita ? "Receita" : "Despesa";
    }
}
