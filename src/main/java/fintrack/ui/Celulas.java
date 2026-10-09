package fintrack.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.function.Function;

/**
 * Fábrica genérica de células de tabela formatadas.
 * <p>
 * Um único método atende colunas de qualquer tipo de linha ({@code S}) e de valor
 * ({@code T}): datas, valores monetários, textos...
 */
public final class Celulas {

    private Celulas() {
        // classe utilitária
    }

    /**
     * @param formatador converte o valor da célula em texto
     * @param classeCss  classe CSS opcional calculada a partir do valor (pode retornar null)
     */
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> formatada(
            Function<? super T, String> formatador,
            Function<? super T, String> classeCss) {

        return coluna -> new TableCell<>() {
            private String classeAtual;

            @Override
            protected void updateItem(T item, boolean vazio) {
                super.updateItem(item, vazio);
                if (classeAtual != null) {
                    getStyleClass().remove(classeAtual);
                    classeAtual = null;
                }
                if (vazio || item == null) {
                    setText(null);
                    return;
                }
                setText(formatador.apply(item));
                String classe = classeCss == null ? null : classeCss.apply(item);
                if (classe != null) {
                    getStyleClass().add(classe);
                    classeAtual = classe;
                }
            }
        };
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> formatada(Function<? super T, String> formatador) {
        return formatada(formatador, null);
    }
}
