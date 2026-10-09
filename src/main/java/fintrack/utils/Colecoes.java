package fintrack.utils;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

/**
 * Métodos genéricos reutilizáveis que demonstram os três tipos de curinga.
 *
 * <table>
 *   <caption>Resumo (PECS - Producer Extends, Consumer Super)</caption>
 *   <tr><th>Curinga</th><th>Uso</th></tr>
 *   <tr><td>{@code ?}</td><td>tipo desconhecido; só operações que não dependem do tipo</td></tr>
 *   <tr><td>{@code ? extends T}</td><td>a coleção <em>produz</em> T (leitura)</td></tr>
 *   <tr><td>{@code ? super T}</td><td>a coleção <em>consome</em> T (escrita)</td></tr>
 * </table>
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

    /** Retorna o elemento na posição (1-based) usada nos menus de console. */
    public static <T> T elementoNaPosicao(List<? extends T> lista, int posicao) {
        if (posicao < 1 || posicao > lista.size()) {
            throw new IndexOutOfBoundsException("Posição inválida: " + posicao);
        }
        return lista.get(posicao - 1);
    }
}
