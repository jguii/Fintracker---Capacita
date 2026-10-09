package fintrack.repository;

import fintrack.model.Identificavel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioGenericoTest {

    static class EntidadeTeste implements Identificavel<Long> {
        private Long id;
        private final String nome;

        EntidadeTeste(String nome) {
            this.nome = nome;
        }

        @Override public Long getId() { return id; }
        @Override public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
    }

    private RepositorioGenerico<EntidadeTeste, Long> repo;

    @BeforeEach
    void setUp() {
        repo = RepositorioGenerico.comIdLong();
    }

    @Test
    void crudBasico() {
        assertTrue(repo.isVazio());
        
        // Create
        EntidadeTeste e1 = new EntidadeTeste("A");
        repo.salvar(e1);
        assertEquals(1L, e1.getId());
        assertEquals(1, repo.contar());

        // Read
        Optional<EntidadeTeste> busca = repo.buscarPorId(1L);
        assertTrue(busca.isPresent());
        assertEquals("A", busca.get().getNome());

        // Update
        EntidadeTeste e1Atualizado = new EntidadeTeste("A-modificado");
        e1Atualizado.setId(1L);
        assertTrue(repo.atualizar(e1Atualizado));
        assertEquals("A-modificado", repo.buscarPorId(1L).get().getNome());

        // Delete
        assertTrue(repo.removerPorId(1L));
        assertTrue(repo.isVazio());
    }

    @Test
    void filtrosEOrdenacaoComCuringas() {
        repo.salvar(new EntidadeTeste("Zebra"));
        repo.salvar(new EntidadeTeste("Abelha"));
        repo.salvar(new EntidadeTeste("Gato"));

        // Ordenar (suporta Comparator<? super T>)
        List<EntidadeTeste> ordenados = repo.listarOrdenado(Comparator.comparing(EntidadeTeste::getNome));
        assertEquals("Abelha", ordenados.get(0).getNome());
        assertEquals("Gato", ordenados.get(1).getNome());
        assertEquals("Zebra", ordenados.get(2).getNome());

        // Filtrar (suporta Predicate<? super T>)
        List<EntidadeTeste> filtrados = repo.filtrar(e -> e.getNome().contains("a"));
        assertEquals(3, filtrados.size()); // Zebra, Abelha e Gato contém 'a'
    }
}
