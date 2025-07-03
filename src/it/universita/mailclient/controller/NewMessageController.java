package it.universita.mailclient.controller;

import it.universita.mailclient.model.Email;
import it.universita.mailclient.utils.EmailValidator;
import it.universita.mailclient.network.ClientSocketManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class NewMessageController {

    @FXML
    private TextField destinatariField;

    @FXML
    private TextField oggettoField;

    @FXML
    private TextArea testoArea;

    private String mittente;

    public void setMittente(String mittente) {
        this.mittente = mittente;
    }

    @FXML
    private void handleSendMessage() {
        String destinatariInput = destinatariField.getText();
        String oggetto = oggettoField.getText();
        String testo = testoArea.getText();

        if (destinatariInput == null || destinatariInput.isBlank() ||
                oggetto == null || oggetto.isBlank() ||
                testo == null || testo.isBlank()) {
            showErrorAlert("Campi vuoti", "Tutti i campi sono obbligatori.");
            return;
        }

        List<String> destinatari = Arrays.asList(destinatariInput.split("\\s*,\\s*"));

        for (String email : destinatari) {
            if (!EmailValidator.validate(email)) {
                showErrorAlert("Errore di validazione", "Il destinatario \"" + email + "\" non è un indirizzo email valido.");
                return;
            }
        }

        if (mittente == null || mittente.isBlank()) {
            showErrorAlert("Errore", "Mittente non definito.");
            return;
        }

        Email nuovaEmail = new Email(0, mittente, destinatari, oggetto, testo, LocalDateTime.now());

        try {
            ClientSocketManager socketManager = new ClientSocketManager("localhost", 12345);

            if (socketManager.connect()) {
                socketManager.sendMessage("SEND_EMAIL");
                System.out.println(" Email da inviare: " + nuovaEmail.toNetworkString());
                socketManager.sendMessage(nuovaEmail.toNetworkString());

                String response = socketManager.receiveMessage();
                socketManager.disconnect();

                if ("OK".equalsIgnoreCase(response)) {
                    showInfoAlert("Email inviata", "Il messaggio è stato inviato con successo!");
                    Stage stage = (Stage) destinatariField.getScene().getWindow();
                    stage.close();
                } else {
                    showErrorAlert("Errore di invio", "Il server ha restituito un errore durante l'invio.");
                }
            } else {
                showErrorAlert("Connessione fallita", "Impossibile connettersi al server.");
            }

        } catch (IOException e) {
            showErrorAlert("Errore di rete", "Errore durante l'invio del messaggio.");
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

    private void showInfoAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //per riempire i campi in automatico
    public void prefillReply(String destinatario, String oggetto) {
        destinatariField.setText(destinatario);
        oggettoField.setText(oggetto);
    }

    //per riempire campi in automatico reply-all
    public void precompilaCampi(List<String> destinatari, String oggetto, String corpo) {
        destinatariField.setText(String.join(", ", destinatari));
        oggettoField.setText(oggetto);
        testoArea.setText(corpo);
    }

    //formato di inoltra
    public void prefillForward(Email email) {
        oggettoField.setText("FWD: " + email.getOggetto());
        testoArea.setText(
                "\n\n---------- Messaggio Inoltrato ----------\n" +
                        "Da: " + email.getMittente() + "\n" +
                        "A: " + String.join(", ", email.getDestinatari()) + "\n" +
                        "Data: " + email.getDataSpedizione() + "\n\n" +
                        email.getTesto()
        );
    }
}