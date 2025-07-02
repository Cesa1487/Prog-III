package it.universita.mailclient.controller;

import it.universita.mailclient.model.User;
import it.universita.mailclient.model.Email;
import it.universita.mailclient.utils.EmailValidator;
import it.universita.mailclient.network.ClientSocketManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginController {

    @FXML
    private TextField emailTextField;

    @FXML
    private void handleLogin() {
        String email = emailTextField.getText();
        System.out.println("Email inserita: " + email);

        if (!EmailValidator.validate(email)) {
            showErrorAlert("Indirizzo email non valido", "Inserisci un indirizzo email corretto.");
            return;
        }

        User user = new User(email);
        System.out.println("Utente creato: " + user.getEmail());

        ClientSocketManager socketManager = new ClientSocketManager("localhost", 12345);
        System.out.println("Tentativo di connessione al server...");

        if (socketManager.connect()) {
            System.out.println("Connessione riuscita!");
            try {
                socketManager.sendMessage(email);
                System.out.println("Email inviata al server.");
                String response = socketManager.receiveMessage();
                System.out.println("Risposta dal server: " + response);

                if (response.equalsIgnoreCase("OK")) {
                    System.out.println("Login riuscito.");
                    socketManager.disconnect();

                    // Nuova connessione per ottenere le email
                    ClientSocketManager emailSocket = new ClientSocketManager("localhost", 12345);
                    if (emailSocket.connect()) {
                        System.out.println("Connessione per GET_EMAILS riuscita");
                        List<Email> emails = getEmailsFromServer(email, emailSocket);
                        emailSocket.disconnect();

                        openInboxWithEmails(email, emails);
                    } else {
                        showErrorAlert("Errore connessione", "Impossibile recuperare le email.");
                    }

                } else {
                    socketManager.disconnect();
                    showErrorAlert("Email non registrata", "L'indirizzo non è presente sul server.");
                }

            } catch (IOException e) {
                showErrorAlert("Errore di rete", "Errore durante la comunicazione col server.");
                e.printStackTrace();
            }
        } else {
            showErrorAlert("Connessione fallita", "Impossibile connettersi al server.");
        }
    }

    // Metodo corretto per leggere TUTTE le righe dal server
    private List<Email> getEmailsFromServer(String userEmail, ClientSocketManager socketManager) throws IOException {
        socketManager.sendMessage("GET_EMAILS:" + userEmail);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socketManager.getSocket().getInputStream()));
        List<String> emailLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                emailLines.add(line);
            }
        }

        List<Email> emails = new ArrayList<>();
        for (String emailLine : emailLines) {
            String[] fields = emailLine.split("\\|");
            if (fields.length >= 6) {
                try {
                    int id = Integer.parseInt(fields[0].trim());
                    String mittente = fields[1].trim();
                    List<String> destinatari = Arrays.asList(fields[2].trim().split(","));
                    String oggetto = fields[3].trim();
                    String testo = fields[4].trim();
                    LocalDateTime data = LocalDateTime.parse(fields[5].trim());

                    emails.add(new Email(id, mittente, destinatari, oggetto, testo, data));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return emails;
    }

    private void openInboxWithEmails(String userEmail, List<Email> emails) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/universita/mailclient/view/inbox_view.fxml"));
            Parent root = loader.load();

            InboxController controller = loader.getController();
            controller.setUserEmail(userEmail);
            controller.setEmails(emails);

            Stage stage = new Stage();
            stage.setTitle("Mail Client - Inbox");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) emailTextField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}