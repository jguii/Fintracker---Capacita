package fintrack.repository;

import fintrack.model.Identificavel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de persistência (CRUD) para qualquer entidade identificável.
 *
 * Implementações:
 * - {@link RepositorioGenerico} — lista em memória (testes / modo offline);
 * - {@link fintrack.dao.TransacaoDAO} — banco de dados via JDBC.
 *
 * Como os serviços dependem desta interface (e não da implementação), trocar a forma
 * de armazenamento não exige nenhuma alteração nas regras de negócio.
 *
 * @param <T>  tipo da entidade — limitado (bounded type) a {@link Identificavel}
 * @param <ID> tipo do identificador
 */
public interface Repositorio<T extends Identificavel<ID>, ID> {

    /** Persiste a entidade, atribuindo-lhe um id. Retorna a própria entidade. */
    T salvar(T entidade);

    /** Atualiza a entidade existente. Retorna {@code false} se o id não existir. */
    boolean atualizar(T entidade);

    /** Remove pelo id. Retorna {@code false} se o id não existir. */
    boolean removerPorId(ID id);

    Optional<T> buscarPorId(ID id);

    List<T> listarTodos();

    int contar();

    /**
     * Salva múltiplos registros em lote.
     *
     * @param entidades registros a serem persistidos
     */
    default void salvarTodos(Collection<? extends T> entidades) {
        for (T entidade : entidades) {
            salvar(entidade);
        }
    }
}
