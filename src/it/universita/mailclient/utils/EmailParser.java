package it.universita.mailclient.utils;

import it.universita.mailclient.model.Email;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmailParser {

    public static List<Email> parse(String response) {
        List<Email> emails = new ArrayList<>();
        if (response == null || response.trim().isEmpty() || response.equalsIgnoreCase("EMPTY")) {
            return emails;
        }

        String[] lines = response.split("\n");
        for (String line : lines) {
            String[] fields = line.split("\\|");
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
                    ex.printStackTrace(); // logga errori di parsing
                }
            }
        }

        return emails;
    }
}