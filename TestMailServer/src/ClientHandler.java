import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private static final Set<String> emailRegistrate = TestMailServer.emailRegistrate;
    private static final Map<String, List<String>> inboxMap = TestMailServer.inboxMap;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        ) {
            String emailRicevuta = reader.readLine();
            if (emailRicevuta == null) {
                writer.write("ERROR\n");
                writer.flush();
                return;
            }

            if (emailRegistrate.contains(emailRicevuta)) {
                writer.write("OK\n");
                MailServerGUI.appendLog("✅ Autenticazione riuscita per " + emailRicevuta);
                writer.flush();
            }
            else if (emailRicevuta.startsWith("GET_EMAILS:")) {
                String destinatario = emailRicevuta.substring("GET_EMAILS:".length()).trim();
                if (!inboxMap.containsKey(destinatario)) {
                    List<String> simulate = new ArrayList<>(TestMailServer.getEmailSimulate(destinatario));
                    inboxMap.put(destinatario, simulate);
                }
                List<String> emails = inboxMap.getOrDefault(destinatario, new ArrayList<>());
                for (String msg : emails) {
                    writer.write(msg + "\n");
                }
                writer.flush();
            }
            else if (emailRicevuta.equals("SEND_EMAIL")) {
                String emailData = reader.readLine();
                if (emailData != null) {
                    String[] parts = emailData.split("\\|");
                    if (parts.length >= 6) {
                        List<String> destinatari = Arrays.asList(parts[2].split(","));
                        boolean tuttiValidi = true;

                        for (String dest : destinatari) {
                            dest = dest.trim();
                            if (!emailRegistrate.contains(dest)) {
                                tuttiValidi = false;
                                break;
                            }
                        }

                        if (tuttiValidi) {
                            for (String dest : destinatari) {
                                dest = dest.trim();
                                ReentrantLock lock = TestMailServer.lockMap.get(dest);
                                lock.lock();
                                try {
                                    List<String> inbox = inboxMap.computeIfAbsent(dest, k -> new ArrayList<>());

                                    // Calcola ID successivo
                                    int nextId = 1;
                                    if (!inbox.isEmpty()) {
                                        String lastEmail = inbox.get(inbox.size() - 1);
                                        String[] lastParts = lastEmail.split("\\|");
                                        try {
                                            int lastId = Integer.parseInt(lastParts[0]);
                                            nextId = lastId + 1;
                                        } catch (NumberFormatException ignored) {}
                                    }

                                    // Ricostruisci messaggio con nuovo ID
                                    String[] emailParts = emailData.split("\\|", 2);
                                    String emailWithCorrectId = nextId + "|" + emailParts[1];

                                    inbox.add(emailWithCorrectId);
                                    TestMailServer.salvaInboxSuFile(dest, inbox);

                                    MailServerGUI.appendLog("📧 Email salvata per " + dest + " con ID " + nextId);
                                } finally {
                                    lock.unlock();
                                }
                            }

                            writer.write("OK\n");
                        } else {
                            writer.write("ERROR\n");
                            MailServerGUI.appendLog("❌ Invio fallito: destinatari non validi");
                        }
                    }
                    else {
                        writer.write("ERROR\n");
                    }
                    writer.flush();
                } else {
                    writer.write("ERROR\n");
                    writer.flush();
                }
            }
            else if (emailRicevuta.startsWith("DELETE_EMAIL:")) {
                String[] parts = emailRicevuta.split(":");
                if (parts.length == 2) {
                    String emailId = parts[1].trim();
                    String destinatario = TestMailServer.getDestinatarioFromEmailId(emailId);
                    if (destinatario != null && inboxMap.containsKey(destinatario)) {
                        ReentrantLock lock = TestMailServer.lockMap.get(destinatario);
                        lock.lock();
                        try {
                            List<String> inbox = inboxMap.get(destinatario);
                            boolean emailRimossa = inbox.removeIf(msg -> msg.contains(emailId));

                            if (emailRimossa) {
                                TestMailServer.salvaInboxSuFile(destinatario, inbox);
                                writer.write("OK\n");
                                MailServerGUI.appendLog("🗑️ Email con ID " + emailId + " cancellata da " + destinatario);
                            } else {
                                writer.write("ERROR: Email non trovata\n");
                                MailServerGUI.appendLog("⚠️ Tentativo di cancellazione fallito: ID " + emailId + " non trovato");
                            }
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        writer.write("ERROR: Email non trovata\n");
                        MailServerGUI.appendLog("⚠️ Tentativo di cancellazione fallito: destinatario per ID " + emailId + " non trovato");
                    }
                    writer.flush();
                } else {
                    writer.write("ERROR: Formato DELETE_EMAIL errato\n");
                    MailServerGUI.appendLog("❌ Formato DELETE_EMAIL errato: " + emailRicevuta);
                    writer.flush();
                }
            }
            else {
                writer.write("ERROR\n");
                writer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}