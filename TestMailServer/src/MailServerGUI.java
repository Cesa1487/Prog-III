import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MailServerGUI extends Application {

    private static TextArea logArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("📬 Mail Server - Log");

        logArea = new TextArea();
        logArea.setEditable(false);

        VBox layout = new VBox(logArea);
        Scene scene = new Scene(layout, 500, 200);

        primaryStage.setScene(scene);
        primaryStage.show();

        appendLog("🟢 Interfaccia Log avviata.");

        //Avvia il server in un thread separato
        new Thread(() -> TestMailServer.main(new String[]{})).start();
    }

    public static void appendLog(String message) {
        javafx.application.Platform.runLater(() -> {
            if (logArea != null) {
                logArea.appendText(message + "\n");
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}