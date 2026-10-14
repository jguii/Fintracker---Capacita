package fintrack.app;

import fintrack.dao.Conexao;
import fintrack.dao.TransacaoDAO;
import fintrack.service.TransacaoService;
import fintrack.ui.Alertas;
import fintrack.ui.GerenciadorTelas;
import fintrack.ui.GerenciadorTelas.Tela;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aplicação JavaFX do FinTrack.
 *
 * Ciclo de vida: {@link #init()} (thread de background: prepara o banco) →
 * {@link #start(Stage)} (thread JavaFX: exibe a tela principal) → {@link #stop()} (libera recursos).
 */
public class FinApp extends Application {

    private static final Logger LOG = Logger.getLogger(FinApp.class.getName());

    private Conexao conexao;
    private GerenciadorTelas telas;
    private Exception erroInicializacao;

    @Override
    public void init() {
        try {
            conexao = Conexao.padrao();
            conexao.inicializarBanco();
            TransacaoService service = new TransacaoService(new TransacaoDAO(conexao));
            telas = new GerenciadorTelas(service);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Falha ao inicializar a aplicação", e);
            erroInicializacao = e;
        }
    }

    @Override
    public void start(Stage palco) {
        if (erroInicializacao != null) {
            Alertas.erro(null, "Não foi possível iniciar o FinTrack", erroInicializacao.getMessage());
            Platform.exit();
            return;
        }

        Tela<Object> principal = telas.carregar(GerenciadorTelas.TELA_PRINCIPAL);
        Parent raiz = principal.raiz();

        palco.setTitle("FinTrack - Controle Financeiro Pessoal");
        palco.setScene(telas.criarCena(raiz));
        palco.setMinWidth(900);
        palco.setMinHeight(560);
        palco.show();
    }

    @Override
    public void stop() {
        if (conexao != null) {
            conexao.close();
        }
        LOG.info("FinTrack encerrado.");
    }
}
