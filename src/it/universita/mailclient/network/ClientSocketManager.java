package it.universita.mailclient.network;

import java.io.*;
import java.net.Socket;

public class ClientSocketManager {
    private String host;
    private int port;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ClientSocketManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //Tentativo di connessione al server
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessage(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    public String receiveMessage() throws IOException {
        return reader.readLine();
    }

    public void disconnect() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }
}