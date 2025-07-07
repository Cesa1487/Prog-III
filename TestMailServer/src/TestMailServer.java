import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

public class TestMailServer {

    public static final Set<String> emailRegistrate = Set.of(
            "samuele@mail.com",
            "riccardo@mail.com",
            "davide@mail.com"
    );

    public static final Map<String, List<String>> inboxMap = new HashMap<>();

    public static final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public static final Set<String> connessioniUniche = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {

        final int PORT = 12345;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            MailServerGUI.appendLog("🟢 Server in ascolto sulla porta " + PORT);

            //Caricamento inbox da file
            for (String email : emailRegistrate) {
                List<String> inbox = caricaInboxDaFile(email);
                inboxMap.put(email, inbox);
                lockMap.put(email, new ReentrantLock());
                MailServerGUI.appendLog("📂 Inbox caricata per " + email + ": " + inbox.size() + " messaggi");
            }

            ExecutorService executor = Executors.newFixedThreadPool(10);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String ip = clientSocket.getInetAddress().toString();
                if (connessioniUniche.add(ip)) {
                    MailServerGUI.appendLog("🔗 Nuova connessione da: " + ip);
                }
                executor.submit(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getEmailSimulate(String email) {
        List<String> simulate = new ArrayList<>();

        if (email.equals("samuele@mail.com")) {
            simulate.add("1|riccardo@mail.com|samuele@mail.com|Benvenuto|Ciao, come va?|2025-03-28T10:30:00");
            simulate.add("2|davide@mail.com|samuele@mail.com|Promemoria|Hai letto il report?|2025-03-28T12:00:00");
        }

        return simulate;
    }

    public static void salvaInboxSuFile(String email, List<String> emailList) {
        ReentrantLock lock = lockMap.get(email);
        lock.lock(); //Acquisisce il lock della inbox

        try {
            File dir = new File("inbox");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, email + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String msg : emailList) {
                    writer.write(msg);
                    writer.newLine();
                }
            } catch (IOException e) {
                MailServerGUI.appendLog("Errore salvataggio inbox di " + email);
                e.printStackTrace();
            }

        } finally {
            lock.unlock(); //Rilascia sempre il lock
        }
    }

    private static List<String> caricaInboxDaFile(String email) {
        List<String> emailList = new ArrayList<>();
        File file = new File("inbox/" + email + ".txt");
        if (!file.exists()) return emailList;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                emailList.add(line);
            }
        } catch (IOException e) {
            MailServerGUI.appendLog("Errore caricamento inbox di " + email);
            e.printStackTrace();
        }

        return emailList;
    }

    public static String getDestinatarioFromEmailId(String emailId) {
        for (Map.Entry<String, List<String>> entry : inboxMap.entrySet()) {
            for (String email : entry.getValue()) {
                if (email.contains(emailId)) {  // Cerca per ID nell'email
                    return entry.getKey();  // Restituisce il destinatario (email dell'utente)
                }
            }
        }
        return null;  // Se l'ID non viene trovato
    }
}