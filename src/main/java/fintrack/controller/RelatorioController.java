package fintrack.controller;

import fintrack.exceptions.EntradaInvalidaException;
import fintrack.exceptions.PersistenciaException;
import fintrack.model.ResumoFinanceiro;
import fintrack.model.Transacao;
import fintrack.service.CalculadoraFinanceira;
import fintrack.service.TransacaoService;
import fintrack.utils.Formatador;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

/**
 * Controller da tela de relatório/saldo ({@code RelatorioView.fxml}).
 */
public class RelatorioController {

    private static final String LINHA = "-".repeat(64);
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
    @FXML private TextArea txtExtrato;
    @FXML private Label lblErro;

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
        ((Stage) txtExtrato.getScene().getWindow()).close();
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
        txtExtrato.setText(montarExtrato(transacoes, resumo, periodo));
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

    private static String montarExtrato(List<Transacao> transacoes, ResumoFinanceiro resumo, String periodo) {
        StringBuilder sb = new StringBuilder();
        sb.append("EXTRATO - ").append(periodo).append('\n').append(LINHA).append('\n');
        if (transacoes.isEmpty()) {
            sb.append("Nenhuma transação no período.\n");
        }
        transacoes.stream()
                .sorted(Comparator.comparing(Transacao::getData).thenComparing(Transacao::getDescricao))
                .forEach(t -> sb.append(String.format("%-10s  %-30.30s %16s%n",
                        Formatador.formatarData(t.getData()),
                        t.getDescricao() + (t.isRecorrente() ? " (mensal)" : ""),
                        Formatador.formatarValor(t.getValorComSinal()))));
        sb.append(LINHA).append('\n')
                .append(String.format("%-42s %16s%n", "Total de receitas:", Formatador.formatarValor(resumo.totalReceitas())))
                .append(String.format("%-42s %16s%n", "Total de despesas:", Formatador.formatarValor(resumo.totalDespesas())))
                .append(String.format("%-42s %16s%n", "SALDO:", Formatador.formatarValor(resumo.saldo())));
        return sb.toString();
    }
}
