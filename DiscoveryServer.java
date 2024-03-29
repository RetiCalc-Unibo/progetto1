import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.io.File;

public class DiscoveryServer {
    // private static final int PORT = 4446;

    public static void main(String[] args) {
        System.out.println("DiscoveryServer: avviato");

        int[] ports = null;
        String[] files = null;

        int porta = -1;
        int nArgs = 0;
        int nServers = 0;

        DatagramSocket socket = null;
        DatagramPacket packet = null;
        byte[] buf = new byte[256];

        RowSwapServer[] swapServers = null;

        // controllo argomenti input: numero dispari di argomenti, almeno 3 (serverPort file1 port1)
        if (args.length%2 == 1 && args.length >= 3) {
            //salvataggio porta per socket server
            try {
                porta = Integer.parseInt(args[0]);
                // controllo che la porta sia nel range consentito 1024-65535
                if (porta < 1024 || porta > 65535) {
                    System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1 [port1>1024] file2 [port2>1024]...");
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1 [port1>1024] file2 [port2>1024]...");
                System.exit(2);
            }

            //porte per SwapRow servers
            //nArgs = args.length / 2;
            ports = new int[args.length / 2];
            files = new String[args.length / 2];

            //ricavo i nomi dei files e i numeri delle porte dagli argomenti
            //controllo che le porte siano accettabili e i file esistano
            try {
                int j = 0, i = 2;
                File f;
                boolean doppia = false;
                int port = -1;

                while (i < args.length) {
                    f = new File(args[i-1]);
                    port = Integer.parseInt(args[i]);

                    // controllo che la porta sia nel range consentito 1024-65535 e che il file esista
                    if (port < 1024 || port > 65535 || !f.isFile()) {
                        System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1 [port1>1024] file2 [port2>1024]...");
                    } else {
                        //controllo non ci siano porte duplicate
                        for (int z = 0; z < nServers; z++) {
                            if (port == ports[z]) {
                                doppia = true;
                                System.out.println("porta doppia: " + port);
                            }
                        }

                        //se la porta non è doppia la inserisco nell'array
                        if (!doppia) {
                            files[j] = args[i-1];
                            ports[j] = port;
                            nServers++;
                            j++;
                        }
                        doppia = false;
                    }
                    i = i + 2;
                }

            } catch (NumberFormatException e) {
                System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1 [port1>1024] file2 [port2>1024]...");
                System.exit(3);
            }
        } else {
            System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1 [port1>1024] file2 [port2>1024]...");
            System.exit(4);
        }

        //creazione e avvio servers SwapRow
        swapServers = new RowSwapServer[nServers];

        for (int i = 0; i < nServers; i++) {
                swapServers[i] = new RowSwapServer(ports[i], files[i]);
                swapServers[i].start();
        }

        //creazione socket server
        try {
            socket = new DatagramSocket(porta);
            packet = new DatagramPacket(buf, buf.length);
            System.out.println("Creata la socket del DS: " + socket);
        } catch (SocketException e) {
            System.out.println("Problemi nella creazione della socket: ");
            e.printStackTrace();
            System.exit(5);
        }

        try {
            String richiesta = null; // nomefile
            int risposta = -1; // porta
            ByteArrayInputStream biStream = null;
            DataInputStream diStream = null;
            StringTokenizer st = null;
            ByteArrayOutputStream boStream = null;
            DataOutputStream doStream = null;
            String linea = null;
            byte[] data = null;

            while (true) {
                System.out.println("\nDS: In attesa di richieste...");

                // ricezione del datagramma
                try {
                    packet.setData(buf);
                    socket.receive(packet);
                } catch (IOException e) {
                    System.err.println("Problemi nella ricezione del datagramma: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio del ciclo
                }

                try {
                    biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    diStream = new DataInputStream(biStream);
                    richiesta = diStream.readUTF();
                    System.out.println("Richiesto file: " + richiesta);
                } catch (Exception e) {
                    System.err.println("Problemi nella lettura della richiesta: " + richiesta);
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio del ciclo
                }

                // preparazione della linea e invio della risposta
                try {
                    boolean trovato = false;
                    //salvataggio porta di risposta

                    for (int i = 0; !trovato && i < nServers; i++) {

                        if (files[i].equals(richiesta)) {
                            trovato = true;
                            risposta = ports[i];
                        }
                    }

                    //rispondo al client
                    boStream = new ByteArrayOutputStream();
                    doStream = new DataOutputStream(boStream);
                    doStream.writeUTF(String.valueOf(risposta));
                    data = boStream.toByteArray();
                    packet.setData(data, 0, data.length);
                    socket.send(packet);
                } catch (IOException e) {
                    System.err.println("Problemi nell'invio della risposta: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio del ciclo
                }
            }
        }

        // qui catturo le eccezioni non catturate all'interno del while in seguito alle quali il server termina l'esecuzione
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("DiscoveryServer: termino...");
        socket.close();
    }
}