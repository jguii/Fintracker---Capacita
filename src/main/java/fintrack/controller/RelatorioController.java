package fintrack.controller;

import fintrack.exceptions.EntradaInvalidaException;
import fintrack.exceptions.PersistenciaException;
import fintrack.model.ResumoFinanceiro;
import fintrack.model.Transacao;
import fintrack.service.CalculadoraFinanceira;
import fintrack.service.TransacaoService;
import fintrack.utils.Formatador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

/**
 * Controller da tela de relatório/saldo ({@code RelatorioView.fxml}).
 */
public class RelatorioController {

    private static final String LINHA = "-".repeat(64);

    private final TransacaoService service;

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;
    @FXML private Label lblReceitas;
    @FXML private Label lblDespesas;
    @FXML private Label lblSaldo;
    @FXML private Label lblQuantidade;
    @FXML private PieChart grafico;
    @FXML private TextArea txtExtrato;
    @FXML private Label lblErro;

    public RelatorioController(TransacaoService service) {
        this.service = service;
    }

    @FXML
    private void initialize() {
        lblErro.visibleProperty().bind(lblErro.textProperty().isNotEmpty());
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

    private void atualizarGrafico(ResumoFinanceiro resumo) {
        String labelReceitas = "Receitas " + Formatador.formatarValor(resumo.totalReceitas());
        String labelDespesas = "Despesas " + Formatador.formatarValor(resumo.totalDespesas());
        double valorReceitas = resumo.totalReceitas().doubleValue();
        double valorDespesas = resumo.totalDespesas().doubleValue();
        double total = valorReceitas + valorDespesas;

        if (grafico.getData().isEmpty()) {
            PieChart.Data receitas = new PieChart.Data(labelReceitas, 0);
            PieChart.Data despesas = new PieChart.Data(labelDespesas, 0);
            PieChart.Data vazio = new PieChart.Data("vazio", total > 0 ? total : 1);
            
            grafico.getData().addAll(receitas, despesas, vazio);

            javafx.application.Platform.runLater(() -> {
                for (javafx.scene.Node n : grafico.lookupAll(".chart-legend-item")) {
                    if (n instanceof Label && "vazio".equals(((Label) n).getText())) {
                        n.setVisible(false);
                        n.setManaged(false);
                    }
                }
            });
        }

        PieChart.Data receitas = grafico.getData().get(0);
        PieChart.Data despesas = grafico.getData().get(1);
        PieChart.Data vazio = grafico.getData().get(2);

        receitas.setName(labelReceitas);
        despesas.setName(labelDespesas);

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1.2),
                        new javafx.animation.KeyValue(receitas.pieValueProperty(), valorReceitas, javafx.animation.Interpolator.LINEAR),
                        new javafx.animation.KeyValue(despesas.pieValueProperty(), valorDespesas, javafx.animation.Interpolator.LINEAR),
                        new javafx.animation.KeyValue(vazio.pieValueProperty(), 0, javafx.animation.Interpolator.LINEAR)
                )
        );
        timeline.play();
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
