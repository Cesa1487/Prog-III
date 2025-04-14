package it.universita.mailclient.controller;

import it.universita.mailclient.model.Email;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class EmailDetailController {

    @FXML
    private Text mittenteText;

    @FXML
    private Text oggettoText;

    @FXML
    private Text dataText;

    @FXML
    private TextArea testoArea;

    public void setEmail(Email email) {
        mittenteText.setText(email.getMittente());
        oggettoText.setText(email.getOggetto());
        dataText.setText(email.getDataSpedizione().toString());
        testoArea.setText(email.getTesto());
    }
}