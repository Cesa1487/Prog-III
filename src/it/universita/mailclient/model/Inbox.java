package it.universita.mailclient.model;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Inbox {

    private ObservableList<Email> emails;

    public Inbox() {
        this.emails = FXCollections.observableArrayList();
    }

    public ObservableList<Email> getEmails() {
        return emails;
    }

    public void addEmail(Email email) {
        emails.add(email);
    }

    public void setEmails(List<Email> newEmails) {
        emails.setAll(newEmails);
    }

    public void removeEmail(Email email) {
        emails.remove(email);
    }
}
