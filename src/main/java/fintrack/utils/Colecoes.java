package fintrack.utils;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

/**
 * Utilitários para operações em coleções genéricas (leitura, escrita e cópia).
 */
public final class Colecoes {

    private Colecoes() {
        // classe utilitária
    }

    /**
     * Curinga ilimitado {@code ?}: funciona para coleções de qualquer tipo,
     * pois só usa {@code Object.toString()}.
     */
    public static void imprimirNumerado(Collection<?> itens, PrintStream saida) {
        int i = 1;
        for (Object item : itens) {
            saida.println(i++ + ". " + item);
        }
    }

    /** Curinga ilimitado: verifica se há elementos, independentemente do tipo. */
    public static boolean isVazia(Collection<?> itens) {
        return itens == null || itens.isEmpty();
    }

    /**
     * Copia de uma origem que produz T para um destino que consome T.
     * Ex.: {@code copiar(List<TransacaoMensal>, List<Transacao>)}.
     *
     * @return quantidade de elementos copiados
     */
    public static <T> int copiar(Collection<? extends T> origem, Collection<? super T> destino) {
        destino.addAll(origem);
        return origem.size();
    }

    /** Retorna o elemento pelo índice 1-based informado. */
    public static <T> T elementoNaPosicao(List<? extends T> lista, int posicao) {
        if (posicao < 1 || posicao > lista.size()) {
            throw new IndexOutOfBoundsException("Posição inválida: " + posicao);
        }
        return lista.get(posicao - 1);
    }
}
