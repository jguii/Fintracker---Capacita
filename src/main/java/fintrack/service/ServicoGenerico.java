package fintrack.service;

import fintrack.exceptions.EntradaInvalidaException;
import fintrack.model.Identificavel;
import fintrack.repository.Repositorio;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Serviço genérico com o fluxo padrão "validar -> persistir" reutilizável para
 * qualquer entidade. Subclasses só precisam implementar {@link #validar(Identificavel)}
 * (padrão Template Method).
 *
 * @param <T>  tipo da entidade
 * @param <ID> tipo do identificador
 */
public abstract class ServicoGenerico<T extends Identificavel<ID>, ID> {

    protected final Repositorio<T, ID> repositorio;

    protected ServicoGenerico(Repositorio<T, ID> repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
    }

    /** Regras de negócio específicas da entidade. */
    protected abstract void validar(T entidade) throws EntradaInvalidaException;

    /** Nome amigável usado nas mensagens de erro (ex.: "Transação"). */
    protected abstract String nomeEntidade();

    public T adicionar(T entidade) throws EntradaInvalidaException {
        exigirNaoNulo(entidade);
        validar(entidade);
        return repositorio.salvar(entidade);
    }

    /** Valida e persiste múltiplos registros em lote. */
    public void adicionarTodos(Collection<? extends T> entidades) throws EntradaInvalidaException {
        for (T entidade : entidades) {
            exigirNaoNulo(entidade);
            validar(entidade);
        }
        repositorio.salvarTodos(entidades);
    }

    public void atualizar(T entidade) throws EntradaInvalidaException {
        exigirNaoNulo(entidade);
        validar(entidade);
        if (!repositorio.atualizar(entidade)) {
            throw new EntradaInvalidaException(nomeEntidade() + " não encontrada: id=" + entidade.getId());
        }
    }

    public void remover(ID id) throws EntradaInvalidaException {
        if (id == null || !repositorio.removerPorId(id)) {
            throw new EntradaInvalidaException(nomeEntidade() + " não encontrada: id=" + id);
        }
    }

    public T buscar(ID id) throws EntradaInvalidaException {
        return repositorio.buscarPorId(id).orElseThrow(
                () -> new EntradaInvalidaException(nomeEntidade() + " não encontrada: id=" + id));
    }

    public List<T> listar() {
        return repositorio.listarTodos();
    }

    /** Filtra registros de acordo com o predicado informado. */
    public List<T> listar(Predicate<? super T> filtro) {
        return repositorio.listarTodos().stream().filter(filtro).toList();
    }

    public int contar() {
        return repositorio.contar();
    }

    private void exigirNaoNulo(T entidade) throws EntradaInvalidaException {
        if (entidade == null) {
            throw new EntradaInvalidaException(nomeEntidade() + " não pode ser nula!");
        }
    }
}
