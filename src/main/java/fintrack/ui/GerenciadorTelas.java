package fintrack.ui;

import fintrack.service.TransacaoService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Carrega telas FXML e injeta dependências nos controllers.
 * <p>
 * O FXML declara o controller com {@code fx:controller} (compatível com o Scene Builder);
 * aqui, a <i>controller factory</i> cria esse controller passando o serviço e o próprio
 * gerenciador pelo construtor — sem variáveis estáticas/globais.
 */
public class GerenciadorTelas {

    public static final String TELA_PRINCIPAL = "MainView.fxml";
    public static final String TELA_TRANSACAO = "TransacaoFormView.fxml";
    public static final String TELA_RELATORIO = "RelatorioView.fxml";

    private static final String PASTA_FXML = "/fintrack/view/";
    private static final String CSS = "/fintrack/css/styles.css";

    private final TransacaoService service;

    /** Par (raiz da tela, controller) com o tipo do controller preservado via generics. */
    public record Tela<C>(Parent raiz, C controller) {
    }

    public GerenciadorTelas(TransacaoService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    /** Carrega um FXML e devolve a raiz e o controller já tipado. */
    public <C> Tela<C> carregar(String arquivoFxml) {
        URL url = Objects.requireNonNull(getClass().getResource(PASTA_FXML + arquivoFxml),
                () -> "FXML não encontrado: " + arquivoFxml);
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(this::criarController);
        try {
            Parent raiz = loader.load();
            return new Tela<>(raiz, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao carregar " + arquivoFxml, e);
        }
    }

    /** Cria uma cena já com a folha de estilos aplicada. */
    public Scene criarCena(Parent raiz) {
        Scene cena = new Scene(raiz);
        cena.getStylesheets().add(Objects.requireNonNull(getClass().getResource(CSS)).toExternalForm());
        return cena;
    }

    /**
     * Abre uma tela em janela modal e aguarda o fechamento.
     *
     * @param configurador recebe o controller antes de exibir a janela
     *                     ({@code ? super C}: aceita um Consumer do controller ou de um supertipo)
     * @return o controller, para consultar o resultado (ex.: se o usuário salvou)
     */
    public <C> C abrirModal(String arquivoFxml, String titulo, Window dono, Consumer<? super C> configurador) {
        Tela<C> tela = carregar(arquivoFxml);
        configurador.accept(tela.controller());

        Stage janela = new Stage();
        janela.setTitle(titulo);
        janela.initOwner(dono);
        janela.initModality(Modality.WINDOW_MODAL);
        janela.setScene(criarCena(tela.raiz()));
        janela.setResizable(true);
        janela.showAndWait();
        return tela.controller();
    }

    /**
     * Procura, em ordem, um construtor (TransacaoService, GerenciadorTelas),
     * depois (TransacaoService) e por fim o construtor sem argumentos.
     */
    private Object criarController(Class<?> tipo) {
        try {
            for (Constructor<?> c : tipo.getConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length == 2 && p[0] == TransacaoService.class && p[1] == GerenciadorTelas.class) {
                    return c.newInstance(service, this);
                }
            }
            for (Constructor<?> c : tipo.getConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length == 1 && p[0] == TransacaoService.class) {
                    return c.newInstance(service);
                }
            }
            return tipo.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Não foi possível criar o controller " + tipo.getName(), e);
        }
    }
}
