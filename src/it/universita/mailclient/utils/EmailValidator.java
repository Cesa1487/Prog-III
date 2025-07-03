package it.universita.mailclient.utils;

import java.util.regex.Pattern;

//Verifica della sintassi indirizzo mail
public class EmailValidator {
    private static final String EMAIL_PATTERN = "^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean validate(String email) {
        if (email == null) {
            return false;
        }
        return pattern.matcher(email).matches();
    }
}
