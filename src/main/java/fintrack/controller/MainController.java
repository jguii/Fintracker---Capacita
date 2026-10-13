package fintrack.controller;

import fintrack.exceptions.EntradaInvalidaException;
import fintrack.exceptions.PersistenciaException;
import fintrack.model.ResumoFinanceiro;
import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import fintrack.model.TransacaoMensal;
import fintrack.service.CalculadoraFinanceira;
import fintrack.service.TransacaoService;
import fintrack.ui.Alertas;
import fintrack.ui.Celulas;
import fintrack.ui.GerenciadorTelas;
import fintrack.utils.Formatador;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Controller da tela principal ({@code MainView.fxml}).
 * <p>
 * Mantém uma {@link ObservableList} com as transações do banco, envolvida por
 * {@link FilteredList} (busca / mês atual) e {@link SortedList} (ordenação pelos cabeçalhos).
 */
public class MainController {

    private final TransacaoService service;
    private final GerenciadorTelas telas;

    private final ObservableList<Transacao> transacoes = FXCollections.observableArrayList();
    private final FilteredList<Transacao> filtradas = new FilteredList<>(transacoes, t -> true);

    @FXML private TableView<Transacao> tabela;
    @FXML private TableColumn<Transacao, LocalDate> colData;
    @FXML private TableColumn<Transacao, String> colDescricao;
    @FXML private TableColumn<Transacao, BigDecimal> colValor;
    @FXML private TableColumn<Transacao, Transacao> colTipo;

    @FXML private Button btnEditar;
    @FXML private Button btnRemover;
    @FXML private TextField txtBusca;
    @FXML private CheckBox chkMesAtual;

    @FXML private Label lblReceitas;
    @FXML private Label lblDespesas;
    @FXML private Label lblSaldo;
    @FXML private Label lblStatus;

    @FXML private VBox painelDetalhes;
    @FXML private Label lblDetalheTipo;
    @FXML private Label lblDetalheRecorrente;
    @FXML private Label lblDetalheValor;
    @FXML private Label lblDetalheDescricao;
    @FXML private Label lblDetalheData;
    @FXML private VBox boxDetalheDia;
    @FXML private Label lblDetalheDia;
    @FXML private Label lblDetalheObservacao;

    public MainController(TransacaoService service, GerenciadorTelas telas) {
        this.service = service;
        this.telas = telas;
    }

    /** Chamado automaticamente pelo FXMLLoader após injetar os campos {@code @FXML}. */
    @FXML
    private void initialize() {
        configurarColunas();
        configurarLinhas();

        SortedList<Transacao> ordenadas = new SortedList<>(filtradas);
        ordenadas.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenadas);

        // Botões só ficam habilitados com uma linha selecionada
        BooleanBinding semSelecao = tabela.getSelectionModel().selectedItemProperty().isNull();
        btnEditar.disableProperty().bind(semSelecao);
        btnRemover.disableProperty().bind(semSelecao);

        painelDetalhes.managedProperty().bind(painelDetalhes.visibleProperty());
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionada) -> {
            if (selecionada != null) {
                exibirDetalhes(selecionada);
            } else {
                fecharDetalhes();
            }
        });

        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        chkMesAtual.selectedProperty().addListener((obs, antigo, novo) -> aplicarFiltro());

        recarregar();
    }

    private void configurarColunas() {
        colData.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getData()));
        colData.setCellFactory(Celulas.formatada(Formatador::formatarData));

        colDescricao.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getDescricao()));

        colValor.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getValorComSinal()));
        colValor.setCellFactory(Celulas.formatada(
                Formatador::formatarValor,
                valor -> valor.signum() >= 0 ? "valor-positivo" : "valor-negativo"));

        colTipo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colTipo.setCellFactory(Celulas.formatada(
                Transacao::getRotuloTipo,
                t -> t.isReceita() ? "tipo-receita" : "tipo-despesa"));
        colTipo.setComparator((a, b) -> a.getRotuloTipo().compareTo(b.getRotuloTipo()));

        colData.setSortType(TableColumn.SortType.DESCENDING);
        tabela.getSortOrder().add(colData);
    }

    /** Duplo clique em uma linha abre a edição; clique simples exibe detalhes; linhas mensais recebem destaque. */
    private void configurarLinhas() {
        tabela.setRowFactory(tv -> {
            TableRow<Transacao> linha = new TableRow<>() {
                @Override
                protected void updateItem(Transacao item, boolean vazio) {
                    super.updateItem(item, vazio);
                    getStyleClass().remove("linha-recorrente");
                    if (!vazio && item != null && item.isRecorrente()) {
                        getStyleClass().add("linha-recorrente");
                    }
                }
            };
            linha.setOnMouseClicked(evento -> {
                if (evento.getButton() == MouseButton.PRIMARY && !linha.isEmpty()) {
                    if (evento.getClickCount() == 2) {
                        abrirFormulario(linha.getItem(), null);
                    } else if (evento.getClickCount() == 1) {
                        exibirDetalhes(linha.getItem());
                    }
                }
            });
            return linha;
        });
    }

    // ------------------------------------------------------------ ações (setOnAction via FXML)

    @FXML
    private void onNovaReceita() {
        abrirFormulario(null, TipoTransacao.RECEITA);
    }

    @FXML
    private void onNovaDespesa() {
        abrirFormulario(null, TipoTransacao.DESPESA);
    }

    @FXML
    private void onEditar() {
        Transacao selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            abrirFormulario(selecionada, null);
        }
    }

    @FXML
    private void onRemover() {
        Transacao selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            return;
        }
        boolean confirmado = Alertas.confirmar(janela(), "Remover transação?",
                selecionada.getDescricao() + " — " + Formatador.formatarValor(selecionada.getValor())
                        + "\n\nEsta ação não pode ser desfeita.");
        if (!confirmado) {
            return;
        }
        try {
            service.remover(selecionada.getId());
            recarregar();
            lblStatus.setText("Transação removida.");
        } catch (EntradaInvalidaException | PersistenciaException e) {
            Alertas.erro(janela(), "Não foi possível remover", e.getMessage());
        }
    }

    @FXML
    private void onRelatorio() {
        telas.<RelatorioController>abrirModal(GerenciadorTelas.TELA_RELATORIO,
                "FinTrack - Relatório", janela(), RelatorioController::exibirMesAtual);
    }

    @FXML
    private void onAtualizar() {
        recarregar();
    }

    @FXML
    private void onFecharDetalhes() {
        fecharDetalhes();
    }

    // -------------------------------------------------------------- auxiliares

    /** Abre o formulário: {@code existente != null} = edição; caso contrário, nova transação. */
    private void abrirFormulario(Transacao existente, TipoTransacao tipoNovo) {
        String titulo = existente == null ? "Nova " + tipoNovo.getDescricao().toLowerCase(Locale.ROOT) : "Editar transação";
        TransacaoFormController form = telas.abrirModal(GerenciadorTelas.TELA_TRANSACAO, "FinTrack - " + titulo,
                janela(), (TransacaoFormController c) -> {
                    if (existente == null) {
                        c.prepararNova(tipoNovo);
                    } else {
                        c.prepararEdicao(existente);
                    }
                });
        if (form.isSalvo()) {
            recarregar();
            lblStatus.setText(existente == null ? "Transação adicionada." : "Transação atualizada.");
        }
    }

    private void exibirDetalhes(Transacao t) {
        if (t == null) {
            fecharDetalhes();
            return;
        }

        lblDetalheTipo.setText(t.isReceita() ? "Receita" : "Despesa");
        lblDetalheTipo.getStyleClass().setAll("badge-detalhe", t.isReceita() ? "badge-receita" : "badge-despesa");

        lblDetalheRecorrente.setVisible(t.isRecorrente());
        lblDetalheRecorrente.setManaged(t.isRecorrente());

        lblDetalheValor.setText(Formatador.formatarValor(t.getValorComSinal()));
        lblDetalheValor.getStyleClass().setAll("detalhe-valor", t.isReceita() ? "valor-positivo" : "valor-negativo");

        lblDetalheDescricao.setText(t.getDescricao());
        lblDetalheData.setText(Formatador.formatarData(t.getData()));

        if (t instanceof TransacaoMensal) {
            TransacaoMensal tm = (TransacaoMensal) t;
            lblDetalheDia.setText("Dia " + tm.getDiaDoMes());
            boxDetalheDia.setVisible(true);
            boxDetalheDia.setManaged(true);
        } else {
            boxDetalheDia.setVisible(false);
            boxDetalheDia.setManaged(false);
        }

        String obs = t.getObservacao();
        if (obs != null && !obs.isBlank()) {
            lblDetalheObservacao.setText(obs);
            lblDetalheObservacao.getStyleClass().setAll("observacao-texto");
        } else {
            lblDetalheObservacao.setText("Nenhuma observação informada.");
            lblDetalheObservacao.getStyleClass().setAll("observacao-texto", "observacao-vazia");
        }

        painelDetalhes.setVisible(true);
    }

    private void fecharDetalhes() {
        painelDetalhes.setVisible(false);
    }

    private void recarregar() {
        try {
            Transacao selecionadaAntes = tabela.getSelectionModel().getSelectedItem();
            Long idAntes = selecionadaAntes != null ? selecionadaAntes.getId() : null;

            transacoes.setAll(service.listar());
            atualizarResumo();

            if (idAntes != null) {
                for (Transacao t : tabela.getItems()) {
                    if (idAntes.equals(t.getId())) {
                        tabela.getSelectionModel().select(t);
                        exibirDetalhes(t);
                        return;
                    }
                }
            }
            fecharDetalhes();
        } catch (PersistenciaException e) {
            Alertas.erro(janela(), "Erro ao carregar transações", e.getMessage());
        }
    }

    private void aplicarFiltro() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().trim().toLowerCase(Locale.ROOT);
        YearMonth mesAtual = YearMonth.now();

        Predicate<Transacao> porTexto = t -> termo.isEmpty()
                || t.getDescricao().toLowerCase(Locale.ROOT).contains(termo);
        Predicate<Transacao> porMes = t -> !chkMesAtual.isSelected()
                || YearMonth.from(t.getData()).equals(mesAtual);

        filtradas.setPredicate(porTexto.and(porMes));
        atualizarResumo();
    }

    /** Resumo calculado sobre as transações exibidas (respeita os filtros). */
    private void atualizarResumo() {
        ResumoFinanceiro resumo = CalculadoraFinanceira.resumir(filtradas);
        lblReceitas.setText(Formatador.formatarValor(resumo.totalReceitas()));
        lblDespesas.setText(Formatador.formatarValor(resumo.totalDespesas()));
        lblSaldo.setText(Formatador.formatarValor(resumo.saldo()));
        lblSaldo.getStyleClass().removeAll("valor-positivo", "valor-negativo");
        lblSaldo.getStyleClass().add(resumo.isPositivo() ? "valor-positivo" : "valor-negativo");
        lblStatus.setText(filtradas.size() == transacoes.size()
                ? transacoes.size() + " transação(ões)"
                : "Exibindo " + filtradas.size() + " de " + transacoes.size());
    }

    private Window janela() {
        return tabela.getScene() == null ? null : tabela.getScene().getWindow();
    }
}
