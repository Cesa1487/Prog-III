package it.universita.mailclient.controller;

import java.util.List;

import it.universita.mailclient.model.Email;
import it.universita.mailclient.model.Inbox;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.IOException;

public class InboxController {

    private String userEmail;

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    private ListView<Email> emailListView;

    private Inbox inbox;

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
}