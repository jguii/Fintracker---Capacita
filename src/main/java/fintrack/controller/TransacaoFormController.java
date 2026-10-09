package fintrack.controller;

import fintrack.exceptions.EntradaInvalidaException;
import fintrack.exceptions.PersistenciaException;
import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import fintrack.model.TransacaoMensal;
import fintrack.service.TransacaoService;
import fintrack.utils.Formatador;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Controller do formulário de transação ({@code TransacaoFormView.fxml}).
 * Usado tanto para inclusão quanto para edição.
 */
public class TransacaoFormController {

    private static final String CLASSE_INVALIDO = "campo-invalido";

    private final TransacaoService service;

    /** Transação em edição; {@code null} quando é uma inclusão. */
    private Transacao emEdicao;
    private boolean salvo;

    @FXML private Label lblTitulo;
    @FXML private RadioButton rbReceita;
    @FXML private RadioButton rbDespesa;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtValor;
    @FXML private DatePicker dpData;
    @FXML private CheckBox chkMensal;
    @FXML private Spinner<Integer> spDia;
    @FXML private TextArea txtObservacao;
    @FXML private Label lblErro;

    public TransacaoFormController(TransacaoService service) {
        this.service = service;
    }

    @FXML
    private void initialize() {
        spDia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, LocalDate.now().getDayOfMonth()));
        spDia.disableProperty().bind(chkMensal.selectedProperty().not());

        dpData.setConverter(new ConversorData());
        dpData.setValue(LocalDate.now());
        // Ao marcar "mensal", sugere o dia da data escolhida
        chkMensal.selectedProperty().addListener((obs, antes, marcado) -> {
            if (marcado && dpData.getValue() != null) {
                spDia.getValueFactory().setValue(dpData.getValue().getDayOfMonth());
            }
        });

        lblErro.managedProperty().bind(lblErro.visibleProperty());

        // Remove o destaque de erro assim que o usuário corrige o campo
        txtDescricao.textProperty().addListener((o, a, n) -> txtDescricao.getStyleClass().remove(CLASSE_INVALIDO));
        txtValor.textProperty().addListener((o, a, n) -> txtValor.getStyleClass().remove(CLASSE_INVALIDO));
        dpData.valueProperty().addListener((o, a, n) -> dpData.getStyleClass().remove(CLASSE_INVALIDO));
    }

    // ------------------------------------------------- preparação (chamado pelo MainController)

    public void prepararNova(TipoTransacao tipo) {
        emEdicao = null;
        selecionarTipo(tipo);
        lblTitulo.setText("Nova " + tipo.getDescricao().toLowerCase());
    }

    public void prepararEdicao(Transacao transacao) {
        emEdicao = transacao;
        lblTitulo.setText("Editar transação");
        selecionarTipo(transacao.getTipo());
        txtDescricao.setText(transacao.getDescricao());
        txtValor.setText(transacao.getValor().toPlainString().replace('.', ','));
        dpData.setValue(transacao.getData());
        txtObservacao.setText(transacao.getObservacao());
        if (transacao instanceof TransacaoMensal mensal) {
            chkMensal.setSelected(true);
            spDia.getValueFactory().setValue(mensal.getDiaDoMes());
        }
    }

    public boolean isSalvo() {
        return salvo;
    }

    // ---------------------------------------------------------------- ações

    @FXML
    private void onSalvar() {
        limparErros();
        try {
            Transacao transacao = montarTransacao();
            if (emEdicao == null) {
                service.adicionar(transacao);
            } else {
                transacao.setId(emEdicao.getId());
                service.atualizar(transacao);
            }
            salvo = true;
            fechar();
        } catch (CampoInvalidoException e) {
            mostrarErro(e.getMessage(), e.campo);
        } catch (IllegalArgumentException | EntradaInvalidaException e) {
            mostrarErro(e.getMessage(), null);
        } catch (PersistenciaException e) {
            mostrarErro("Erro ao gravar no banco de dados: " + e.getMessage(), null);
        }
    }

    @FXML
    private void onCancelar() {
        fechar();
    }

    // ------------------------------------------------------------ auxiliares

    /** Lê e valida os campos, indicando qual campo está inválido. */
    private Transacao montarTransacao() throws CampoInvalidoException {
        String descricao = txtDescricao.getText();
        if (descricao == null || descricao.isBlank()) {
            throw new CampoInvalidoException("Informe a descrição.", txtDescricao);
        }

        BigDecimal valor;
        try {
            valor = Formatador.parseValor(txtValor.getText());
        } catch (NumberFormatException e) {
            throw new CampoInvalidoException("Valor inválido. Use o formato 1234,56.", txtValor);
        }
        if (valor.signum() <= 0) {
            throw new CampoInvalidoException("O valor deve ser maior que zero.", txtValor);
        }

        LocalDate data = lerData();
        TipoTransacao tipo = rbReceita.isSelected() ? TipoTransacao.RECEITA : TipoTransacao.DESPESA;
        String observacao = txtObservacao.getText();

        if (chkMensal.isSelected()) {
            return new TransacaoMensal(descricao, valor, tipo, data, observacao, lerDia());
        }
        return new Transacao(descricao, valor, tipo, data, observacao);
    }

    /** Considera também o texto digitado no DatePicker que ainda não foi "confirmado". */
    private LocalDate lerData() throws CampoInvalidoException {
        String digitado = dpData.getEditor().getText();
        if (digitado != null && !digitado.isBlank()) {
            try {
                LocalDate data = Formatador.parseData(digitado);
                dpData.setValue(data);
                return data;
            } catch (DateTimeParseException e) {
                throw new CampoInvalidoException("Data inválida. Use dd/mm/aaaa.", dpData);
            }
        }
        if (dpData.getValue() == null) {
            throw new CampoInvalidoException("Informe a data.", dpData);
        }
        return dpData.getValue();
    }

    /** O Spinner editável só confirma o texto ao pressionar Enter; aqui forçamos a leitura. */
    private int lerDia() throws CampoInvalidoException {
        try {
            int dia = Integer.parseInt(spDia.getEditor().getText().trim());
            if (dia < 1 || dia > 31) {
                throw new NumberFormatException();
            }
            spDia.getValueFactory().setValue(dia);
            return dia;
        } catch (NumberFormatException e) {
            throw new CampoInvalidoException("O dia do mês deve estar entre 1 e 31.", spDia);
        }
    }

    private void selecionarTipo(TipoTransacao tipo) {
        (tipo == TipoTransacao.RECEITA ? rbReceita : rbDespesa).setSelected(true);
    }

    private void mostrarErro(String mensagem, Control campo) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
        if (campo != null) {
            campo.getStyleClass().add(CLASSE_INVALIDO);
            campo.requestFocus();
        }
    }

    private void limparErros() {
        lblErro.setVisible(false);
        for (Control c : new Control[]{txtDescricao, txtValor, dpData, spDia}) {
            c.getStyleClass().remove(CLASSE_INVALIDO);
        }
    }

    private void fechar() {
        ((Stage) lblTitulo.getScene().getWindow()).close();
    }

    /** Erro de preenchimento associado a um campo específico da tela. */
    private static final class CampoInvalidoException extends Exception {
        private final transient Control campo;

        CampoInvalidoException(String mensagem, Control campo) {
            super(mensagem);
            this.campo = campo;
        }
    }

    /** Converte datas no formato dd/MM/yyyy sem lançar exceção em textos inválidos. */
    private static final class ConversorData extends StringConverter<LocalDate> {
        @Override
        public String toString(LocalDate data) {
            return Formatador.formatarData(data);
        }

        @Override
        public LocalDate fromString(String texto) {
            if (texto == null || texto.isBlank()) {
                return null;
            }
            try {
                return Formatador.parseData(texto);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
