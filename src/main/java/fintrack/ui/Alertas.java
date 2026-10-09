package fintrack.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/** Atalhos para diálogos padronizados. */
public final class Alertas {

    private Alertas() {
        // classe utilitária
    }

    public static void erro(Window dono, String titulo, String mensagem) {
        criar(Alert.AlertType.ERROR, dono, titulo, mensagem).showAndWait();
    }

    public static void info(Window dono, String titulo, String mensagem) {
        criar(Alert.AlertType.INFORMATION, dono, titulo, mensagem).showAndWait();
    }

    public static boolean confirmar(Window dono, String titulo, String mensagem) {
        Optional<ButtonType> resposta = criar(Alert.AlertType.CONFIRMATION, dono, titulo, mensagem).showAndWait();
        return resposta.filter(ButtonType.OK::equals).isPresent();
    }

    private static Alert criar(Alert.AlertType tipo, Window dono, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo, mensagem);
        alerta.setTitle("FinTrack");
        alerta.setHeaderText(titulo);
        if (dono != null) {
            alerta.initOwner(dono);
        }
        return alerta;
    }
}
