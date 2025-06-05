package it.universita.mailclient.controller;

import it.universita.mailclient.model.Email;
import it.universita.mailclient.model.Inbox;

import it.universita.mailclient.network.ClientSocketManager;

import it.universita.mailclient.utils.EmailParser;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;

public class InboxController {

    private String userEmail;

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    private ListView<Email> emailListView;

    private Inbox inbox;
    private List<Email> ultimaListaEmail = new ArrayList<>();

    @FXML
    public void initialize() {
        inbox = new Inbox();
        emailListView.setItems(inbox.getEmails());

        // ✅ Listener per doppio clic su una mail
        emailListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Email selectedEmail = emailListView.getSelectionModel().getSelectedItem();
                if (selectedEmail != null) {
                    openEmailDetail(selectedEmail);
                }
            }
        });

        avviaAggiornamentoAutomatico();
    }

    public void setEmails(List<Email> emails) {
        inbox = new Inbox();
        inbox.setEmails(emails);
        emailListView.setItems(inbox.getEmails());
    }

    private void openEmailDetail(Email email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/universita/mailclient/view/email_detail_view.fxml"));
            Parent root = loader.load();

            EmailDetailController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = new Stage();
            stage.setTitle("Dettagli Email");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openNewMessage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/universita/mailclient/view/new_message_view.fxml"));
            Parent root = loader.load();

            // Passiamo l'email dell'utente loggato al controller del messaggio
            NewMessageController controller = loader.getController();
            controller.setMittente(userEmail);

            Stage stage = new Stage();
            stage.setTitle("Nuovo Messaggio");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteEmail() {
        Email selected = emailListView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            inbox.getEmails().remove(selected);
            System.out.println("✂️ Email eliminata: " + selected.getOggetto());
        } else {
            // Mostra popup invece che solo nel terminale
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText("Nessuna email selezionata");
            alert.setContentText("Seleziona un'email prima di premere Elimina.");
            alert.showAndWait();
        }
    }

    //metodo per fare logout e accedere da un'altra mail
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/universita/mailclient/view/login_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.show();

            // Chiude la finestra attuale (Inbox)
            Stage currentStage = (Stage) emailListView.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Impossibile tornare alla schermata di login");
            alert.setContentText("Errore: " + e.getMessage());
            alert.showAndWait();
        }
    }

    //funzione di rispondi nella inbox
    @FXML
    private void handleReplyEmail() {
        Email selected = emailListView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/universita/mailclient/view/new_message_view.fxml"));
                Parent root = loader.load();

                NewMessageController controller = loader.getController();

                // Pre-compila destinatario e oggetto (con "Re:" come prefisso)
                controller.setMittente(userEmail);
                controller.prefillReply(selected.getMittente(), "Re: " + selected.getOggetto());

                Stage stage = new Stage();
                stage.setTitle("Rispondi al messaggio");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText("Nessuna email selezionata");
            alert.setContentText("Seleziona una mail per rispondere.");
            alert.showAndWait();
        }
    }

    //funzione di rispondi a tutti nella inbox
    @FXML
    private void handleReplyAll() {
        Email selected = emailListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText("Nessuna email selezionata");
            alert.setContentText("Seleziona un'email per rispondere a tutti.");
            alert.showAndWait();
            return;
        }

        List<String> destinatari = new ArrayList<>();
        destinatari.add(selected.getMittente());

        for (String d : selected.getDestinatari()) {
            if (!d.equalsIgnoreCase(userEmail) && !destinatari.contains(d)) {
                destinatari.add(d);
            }
        }

        apriFinestraRisposta(destinatari, "Re: " + selected.getOggetto(), "\n\n--- Messaggio originale ---\n" + selected.getTesto());
    }

    //finestra di risposta tutti
    private void apriFinestraRisposta(List<String> destinatari, String oggetto, String corpo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/universita/mailclient/view/new_message_view.fxml"));
            Parent root = loader.load();

            NewMessageController controller = loader.getController();
            controller.setMittente(userEmail);
            controller.precompilaCampi(destinatari, oggetto, corpo);

            Stage stage = new Stage();
            stage.setTitle("Rispondi a tutti");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //funzione di inoltra messaggio
    @FXML
    private void handleForwardEmail() {
        Email selected = emailListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/universita/mailclient/view/new_message_view.fxml"));
                Parent root = loader.load();

                NewMessageController controller = loader.getController();
                controller.setMittente(userEmail);
                controller.prefillForward(selected); // ✅ metodo da aggiungere nel NewMessageController

                Stage stage = new Stage();
                stage.setTitle("Inoltra Messaggio");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText("Nessuna email selezionata");
            alert.setContentText("Seleziona un'email da inoltrare.");
            alert.showAndWait();
        }
    }

    @FXML
    private Label connessioneLabel;

    private Timeline refreshTimeline;

    private void aggiornaStatoConnessione(boolean connesso) {
        if (connessioneLabel != null) {
            connessioneLabel.setText(connesso ? " Connesso al server" : " Server non raggiungibile");
            connessioneLabel.setStyle(connesso ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        }
    }

    private void aggiornaInbox() {
        try {
            ClientSocketManager socket = new ClientSocketManager("localhost", 12345);
            if (socket.connect()) {
                aggiornaStatoConnessione(true);
                socket.sendMessage("GET_EMAILS:" + userEmail);

                // ✅ Lettura di tutte le righe dal server
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getSocket().getInputStream())
                );
                StringBuilder fullResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    fullResponse.append(line).append("\n");
                }

                socket.disconnect();
                String response = fullResponse.toString();

                // Log della risposta grezza ricevuta dal server
                System.out.println("Risposta grezza dal server:\n" + response);

                List<Email> nuoveEmail = EmailParser.parse(response);

                // 🔍 Log delle email effettivamente parseate
                System.out.println("📨 Email ricevute dal server:");
                for (Email email : nuoveEmail) {
                    System.out.println(" - " + email.getOggetto() + " da " + email.getMittente());
                }

                // ⚠️ Solo se ci sono nuove email aggiungile
                if (!nuoveEmail.equals(ultimaListaEmail)) {
                    emailListView.getItems().setAll(nuoveEmail);

                    // 📢 Mostra notifica popup
                    if (!ultimaListaEmail.isEmpty() && nuoveEmail.size() > ultimaListaEmail.size()) {
                        int nuoviMessaggi = nuoveEmail.size() - ultimaListaEmail.size();
                        showNewEmailAlert(nuoviMessaggi);
                    }

                    ultimaListaEmail = new ArrayList<>(nuoveEmail);
                }

            } else {
                aggiornaStatoConnessione(false);
            }
        } catch (IOException e) {
            aggiornaStatoConnessione(false);
            System.out.println("Errore durante aggiornamento inbox: " + e.getMessage());
        }
    }

    private void avviaAggiornamentoAutomatico() {
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> aggiornaInbox())
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void showNewEmailAlert(int quanti) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📬 Nuove Email");
        alert.setHeaderText(null);
        alert.setContentText("Hai ricevuto " + quanti + " nuove email.");
        alert.show();
    }
}