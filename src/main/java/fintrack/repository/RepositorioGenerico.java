package fintrack.repository;

import fintrack.model.Identificavel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Implementação genérica de repositório em memória para entidades identificáveis.
 *
 * <pre>{@code
 * Repositorio<Transacao, Long> repo = RepositorioGenerico.comIdLong();
 * repo.salvar(new Transacao("Salário", new BigDecimal("3000"), RECEITA, LocalDate.now()));
 * }</pre>
 *
 * @param <T>  tipo da entidade
 * @param <ID> tipo do identificador
 */
public class RepositorioGenerico<T extends Identificavel<ID>, ID> implements Repositorio<T, ID> {

    /** LinkedHashMap preserva a ordem de inserção e permite busca O(1) por id. */
    private final Map<ID, T> registros = new LinkedHashMap<>();
    private final Supplier<? extends ID> geradorDeId;

    /**
     * @param geradorDeId estratégia de geração de identificadores
     */
    public RepositorioGenerico(Supplier<? extends ID> geradorDeId) {
        this.geradorDeId = Objects.requireNonNull(geradorDeId, "geradorDeId");
    }

    /** Fábrica de conveniência: ids Long sequenciais (1, 2, 3...). */
    public static <E extends Identificavel<Long>> RepositorioGenerico<E, Long> comIdLong() {
        AtomicLong sequencia = new AtomicLong();
        return new RepositorioGenerico<>(sequencia::incrementAndGet);
    }

    // ------------------------------------------------------------------ CRUD

    @Override
    public T salvar(T entidade) {
        Objects.requireNonNull(entidade, "A entidade não pode ser nula!");
        if (entidade.getId() == null) {
            entidade.setId(geradorDeId.get());
        }
        registros.put(entidade.getId(), entidade);
        return entidade;
    }

    @Override
    public boolean atualizar(T entidade) {
        Objects.requireNonNull(entidade, "A entidade não pode ser nula!");
        if (entidade.getId() == null || !registros.containsKey(entidade.getId())) {
            return false;
        }
        registros.put(entidade.getId(), entidade);
        return true;
    }

    @Override
    public boolean removerPorId(ID id) {
        return registros.remove(id) != null;
    }

    /** Remove a entidade informada (pelo id). */
    public boolean remover(T entidade) {
        return entidade != null && removerPorId(entidade.getId());
    }

    @Override
    public Optional<T> buscarPorId(ID id) {
        return Optional.ofNullable(registros.get(id));
    }

    /** Retorna uma cópia defensiva — alterações na lista não afetam o repositório. */
    @Override
    public List<T> listarTodos() {
        return new ArrayList<>(registros.values());
    }

    @Override
    public int contar() {
        return registros.size();
    }

    public boolean isVazio() {
        return registros.isEmpty();
    }

    public void limpar() {
        registros.clear();
    }

    // ------------------------------------------------ operações de consulta
    public List<T> filtrar(Predicate<? super T> criterio) {
        List<T> resultado = new ArrayList<>();
        for (T registro : registros.values()) {
            if (criterio.test(registro)) {
                resultado.add(registro);
            }
        }
        return resultado;
    }

    /** Lista ordenada por qualquer comparador do tipo ou de um supertipo. */
    public List<T> listarOrdenado(Comparator<? super T> comparador) {
        List<T> copia = listarTodos();
        copia.sort(comparador);
        return copia;
    }

    /**
     * Copia todos os registros para a coleção de destino fornecida.
     *
     * @param destino coleção que receberá os registros
     */
    public void copiarPara(Collection<? super T> destino) {
        destino.addAll(registros.values());
    }

    /** Projeta cada registro em outro tipo (ex.: transação -> descrição). */
    public <R> List<R> mapear(Function<? super T, ? extends R> mapeador) {
        List<R> resultado = new ArrayList<>(registros.size());
        for (T registro : registros.values()) {
            resultado.add(mapeador.apply(registro));
        }
        return resultado;
    }
}
