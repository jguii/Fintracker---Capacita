package fintrack.controller;

import fintrack.exceptions.EntradaInvalidaException;
import fintrack.exceptions.PersistenciaException;
import fintrack.model.ResumoFinanceiro;
import fintrack.model.Transacao;
import fintrack.service.CalculadoraFinanceira;
import fintrack.service.GeradorExtrato;
import fintrack.service.TransacaoService;
import fintrack.utils.Formatador;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;

/**
 * Controller da tela de relatório/saldo ({@code RelatorioView.fxml}).
 */
public class RelatorioController {

    private static final Duration DURACAO_ANIMACAO = Duration.seconds(1.0);

    private final TransacaoService service;

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;
    @FXML private Label lblReceitas;
    @FXML private Label lblDespesas;
    @FXML private Label lblSaldo;
    @FXML private Label lblQuantidade;
    @FXML private PieChart grafico;
    @FXML private Label lblLegendaReceitas;
    @FXML private Label lblLegendaDespesas;
    @FXML private Label lblTituloExtrato;
    @FXML private Button btnCopiarExtrato;
    @FXML private VBox boxExtrato;
    @FXML private Label lblErro;

    private List<Transacao> transacoesAtuais;
    private String periodoAtual;

    // Fatias criadas uma única vez: índices fixos => cores fixas (default-color0/1/2 no CSS)
    private PieChart.Data fatiaReceitas;
    private PieChart.Data fatiaDespesas;
    private PieChart.Data fatiaVazia;
    private Timeline animacaoGrafico;

    public RelatorioController(TransacaoService service) {
        this.service = service;
    }

    @FXML
    private void initialize() {
        lblErro.visibleProperty().bind(lblErro.textProperty().isNotEmpty());

        fatiaReceitas = new PieChart.Data("Receitas", 0);
        fatiaDespesas = new PieChart.Data("Despesas", 0);
        fatiaVazia = new PieChart.Data("", 100); // começa todo cinza e é "consumido" pelas outras
        grafico.getData().setAll(fatiaReceitas, fatiaDespesas, fatiaVazia);

        for (PieChart.Data fatia : grafico.getData()) {
            ocultarSeZerada(fatia);
            fatia.nodeProperty().addListener((obs, antigo, novo) -> ocultarSeZerada(fatia));
            fatia.pieValueProperty().addListener((obs, antigo, novo) -> ocultarSeZerada(fatia));
        }
    }

    /** Estado inicial ao abrir a tela. */
    public void exibirMesAtual() {
        onMesAtual();
    }

    @FXML
    private void onMesAtual() {
        YearMonth mes = YearMonth.now();
        gerar(mes.atDay(1), mes.atEndOfMonth());
    }

    @FXML
    private void onAnoAtual() {
        int ano = LocalDate.now().getYear();
        gerar(LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
    }

    @FXML
    private void onTudo() {
        try {
            List<Transacao> todas = service.listar();
            dpInicio.setValue(todas.stream().map(Transacao::getData).min(Comparator.naturalOrder()).orElse(null));
            dpFim.setValue(todas.stream().map(Transacao::getData).max(Comparator.naturalOrder()).orElse(null));
            exibir(todas, "Todo o período");
        } catch (PersistenciaException e) {
            lblErro.setText(e.getMessage());
        }
    }

    @FXML
    private void onGerar() {
        gerar(dpInicio.getValue(), dpFim.getValue());
    }

    @FXML
    private void onFechar() {
        ((Stage) boxExtrato.getScene().getWindow()).close();
    }

    // ------------------------------------------------------------ auxiliares

    private void gerar(LocalDate inicio, LocalDate fim) {
        dpInicio.setValue(inicio);
        dpFim.setValue(fim);
        try {
            List<Transacao> transacoes = service.listarPorPeriodo(inicio, fim);
            exibir(transacoes, Formatador.formatarData(inicio) + " a " + Formatador.formatarData(fim));
        } catch (EntradaInvalidaException | PersistenciaException e) {
            lblErro.setText(e.getMessage());
        }
    }

    private void exibir(List<Transacao> transacoes, String periodo) {
        lblErro.setText("");
        ResumoFinanceiro resumo = CalculadoraFinanceira.resumir(transacoes);

        lblReceitas.setText(Formatador.formatarValor(resumo.totalReceitas()));
        lblDespesas.setText(Formatador.formatarValor(resumo.totalDespesas()));
        lblSaldo.setText(Formatador.formatarValor(resumo.saldo()));
        lblSaldo.getStyleClass().removeAll("valor-positivo", "valor-negativo");
        lblSaldo.getStyleClass().add(resumo.isPositivo() ? "valor-positivo" : "valor-negativo");
        lblQuantidade.setText(String.valueOf(resumo.quantidade()));

        atualizarGrafico(resumo);
        
        this.transacoesAtuais = transacoes;
        this.periodoAtual = periodo;
        montarExtratoVisual(transacoes, resumo, periodo);
    }

    /**
     * Anima o gráfico em velocidade constante.
     * <p>
     * As fatias trabalham em <b>percentual</b> (receitas + despesas + vazio = 100 sempre). Como a soma
     * nunca muda durante a interpolação linear, o ângulo de cada fatia varia de forma perfeitamente
     * uniforme. A animação interna do PieChart fica desligada ({@code animated="false"} no FXML), pois
     * ela aplicaria um EASE_BOTH de 500 ms a cada quadro e distorceria a velocidade.
     */
    private void atualizarGrafico(ResumoFinanceiro resumo) {
        lblLegendaReceitas.setText("Receitas " + Formatador.formatarValor(resumo.totalReceitas()));
        lblLegendaDespesas.setText("Despesas " + Formatador.formatarValor(resumo.totalDespesas()));

        double receitas = resumo.totalReceitas().doubleValue();
        double despesas = resumo.totalDespesas().doubleValue();
        double total = receitas + despesas;

        double alvoReceitas = total > 0 ? receitas / total * 100.0 : 0.0;
        double alvoDespesas = total > 0 ? despesas / total * 100.0 : 0.0;
        double alvoVazio = total > 0 ? 0.0 : 100.0;

        if (animacaoGrafico != null) {
            animacaoGrafico.stop(); // recomeça a partir do ponto atual (a soma continua 100)
        }
        animacaoGrafico = new Timeline(new KeyFrame(DURACAO_ANIMACAO,
                new KeyValue(fatiaReceitas.pieValueProperty(), alvoReceitas, Interpolator.LINEAR),
                new KeyValue(fatiaDespesas.pieValueProperty(), alvoDespesas, Interpolator.LINEAR),
                new KeyValue(fatiaVazia.pieValueProperty(), alvoVazio, Interpolator.LINEAR)));
        animacaoGrafico.play();
    }

    /** Fatias com valor zero ainda desenham a borda (uma linha branca); escondemos nesse caso. */
    private static void ocultarSeZerada(PieChart.Data fatia) {
        Node no = fatia.getNode();
        if (no != null) {
            no.setVisible(fatia.getPieValue() > 0.0001);
        }
    }

    @FXML
    private void onCopiarExtrato() {
        if (transacoesAtuais == null) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(GeradorExtrato.gerarTexto(transacoesAtuais, periodoAtual));
        Clipboard.getSystemClipboard().setContent(content);

        if (btnCopiarExtrato != null) {
            btnCopiarExtrato.setText("Copiado!");
            PauseTransition pausa = new PauseTransition(Duration.seconds(1.5));
            pausa.setOnFinished(e -> btnCopiarExtrato.setText("Copiar extrato"));
            pausa.play();
        }
    }

    private void montarExtratoVisual(List<Transacao> transacoes, ResumoFinanceiro resumo, String periodo) {
        lblTituloExtrato.setText("EXTRATO - " + periodo);
        boxExtrato.getChildren().clear();

        if (transacoes.isEmpty()) {
            Label lblVazio = new Label("Nenhuma transação no período.");
            lblVazio.getStyleClass().add("extrato-texto");
            boxExtrato.getChildren().add(lblVazio);
            return;
        }

        SortedMap<YearMonth, List<Transacao>> porMes = CalculadoraFinanceira.agruparPorMes(transacoes);
        boolean agrupar = porMes.size() > 1;

        for (java.util.Map.Entry<YearMonth, List<Transacao>> entry : porMes.entrySet()) {
            if (agrupar) {
                Label mesLabel = new Label(Formatador.formatarMesAno(entry.getKey()).toUpperCase(Formatador.PT_BR));
                mesLabel.getStyleClass().add("extrato-mes-titulo");
                boxExtrato.getChildren().add(mesLabel);
            }

            boolean impar = false;
            for (Transacao t : entry.getValue()) {
                HBox linha = new HBox(10);
                linha.getStyleClass().add("extrato-linha");
                if (impar) linha.getStyleClass().add("extrato-linha-impar");

                Label lblData = new Label(Formatador.formatarData(t.getData()));
                lblData.setPrefWidth(75);
                lblData.getStyleClass().add("extrato-texto");

                Label lblDesc = new Label(t.getDescricao());
                lblDesc.setMaxWidth(Double.MAX_VALUE);
                lblDesc.getStyleClass().add("extrato-texto");
                HBox.setHgrow(lblDesc, Priority.ALWAYS);

                Label lblValor = new Label(Formatador.formatarValor(t.getValorComSinal()));
                lblValor.setPrefWidth(90);
                lblValor.getStyleClass().add(t.getValorComSinal().signum() >= 0 ? "valor-positivo" : "valor-negativo");
                lblValor.setStyle("-fx-alignment: center-right;");

                linha.getChildren().addAll(lblData, lblDesc, lblValor);

                if (t.isRecorrente()) {
                    Label lblMensal = new Label("Mensal");
                    lblMensal.getStyleClass().add("extrato-etiqueta");
                    linha.getChildren().add(lblMensal);
                } else {
                    // Preencher o espaço para os valores alinharem
                    Region espaco = new Region();
                    espaco.setPrefWidth(45);
                    linha.getChildren().add(espaco);
                }

                boxExtrato.getChildren().add(linha);
                impar = !impar;
            }
        }

        // Rodapé do extrato exibindo apenas o Saldo do período
        HBox linhaSaldo = new HBox(10);
        linhaSaldo.getStyleClass().add("extrato-rodape-saldo");
        Label lblRotuloSaldo = new Label("SALDO:");
        lblRotuloSaldo.getStyleClass().add("extrato-texto-destaque");
        Region espacoSaldo = new Region();
        HBox.setHgrow(espacoSaldo, Priority.ALWAYS);
        Label lblValorSaldo = new Label(Formatador.formatarValor(resumo.saldo()));
        lblValorSaldo.getStyleClass().addAll("extrato-texto-destaque", resumo.isPositivo() ? "valor-positivo" : "valor-negativo");
        linhaSaldo.getChildren().addAll(lblRotuloSaldo, espacoSaldo, lblValorSaldo);
        boxExtrato.getChildren().add(linhaSaldo);
    }
}
