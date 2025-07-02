package it.universita.mailclient.model;

import java.time.LocalDateTime;
import java.util.List;

public class Email {
    private int id;
    private String mittente;
    private List<String> destinatari;
    private String oggetto;
    private String testo;
    private LocalDateTime dataSpedizione;

    public Email(int id, String mittente, List<String> destinatari, String oggetto, String testo, LocalDateTime dataSpedizione) {
        this.id = id;
        this.mittente = mittente;
        this.destinatari = destinatari;
        this.oggetto = oggetto;
        this.testo = testo;
        this.dataSpedizione = dataSpedizione;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMittente() {
        return mittente;
    }

    public void setMittente(String mittente) {
        this.mittente = mittente;
    }

    public List<String> getDestinatari() {
        return destinatari;
    }

    public void setDestinatari(List<String> destinatari) {
        this.destinatari = destinatari;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public LocalDateTime getDataSpedizione() {
        return dataSpedizione;
    }

    public void setDataSpedizione(LocalDateTime dataSpedizione) {
        this.dataSpedizione = dataSpedizione;
    }

    @Override
    public String toString() {
        return oggetto + " - da: " + mittente;
    }

    public String toNetworkString() {
        String testoSanificato = testo.replace("\n", "\\n"); // evita problemi nel protocollo
        return id + "|" +
                mittente + "|" +
                String.join(",", destinatari) + "|" +
                oggetto + "|" +
                testoSanificato + "|" +
                dataSpedizione.toString();
    }
}