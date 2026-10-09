package fintrack.app;

import javafx.application.Application;

/**
 * Ponto de entrada do JAR executável.
 * <p>
 * Quando o JavaFX está no <i>classpath</i> (e não no <i>module path</i>), a JVM recusa
 * iniciar diretamente uma classe que estende {@link Application}
 * ("JavaFX runtime components are missing"). Esta classe intermediária contorna isso.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(FinApp.class, args);
    }
}
