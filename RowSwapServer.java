// LineServer.java

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class RowSwapServer {

    //System.out.println("RowSwap server: avviato");

    DatagramSocket socket = null;
    DatagramPacket packet = null;
    byte[] buf = new byte[256];
    int port = -1;
    String nomeFile;

    public RowSwapServer(int port, String file) throws IOException {
        //NB il controllo del numero della porta viene fatto nel Discovery Server
        this.port = port;
        this.nomeFile = file;
    }

    public void run() {
        try {
            socket = new DatagramSocket(port);
            packet = new DatagramPacket(buf, buf.length);
            System.out.println("Creata la socket: " + socket);
        } catch (SocketException e) {
            System.out.println("Problemi nella creazione della socket: ");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            int numLinea1, numLinea2 = -1;
            String richiesta = null;
            ByteArrayInputStream biStream = null;
            DataInputStream diStream = null;
            StringTokenizer st = null;
            ByteArrayOutputStream boStream = null;
            DataOutputStream doStream = null;
            String linea1, linea2 = null;
            byte[] data = null;

            while (true) {
                System.out.println("\nIn attesa di richieste...");

                // ricezione del datagramma
                try {
                    packet.setData(buf);
                    socket.receive(packet);
                } catch (IOException e) {
                    System.err.println("Problemi nella ricezione del datagramma: "
                            + e.getMessage());
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio
                    // del ciclo
                }

                try {
                    biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    diStream = new DataInputStream(biStream);
                    richiesta = diStream.readUTF();
                    st = new StringTokenizer(richiesta);

                    numLinea1 = Integer.parseInt(st.nextToken());
                    System.out.println("Richiesta linea " + numLinea1 + " del file " + nomeFile);
                    numLinea2 = Integer.parseInt(st.nextToken());
                    System.out.println("Richiesta linea " + numLinea2 + " del file " + nomeFile);

                } catch (Exception e) {
                    System.err.println("Problemi nella lettura della richiesta: "
                            + nomeFile);
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio
                    // del ciclo
                }

                // preparazione della linea e invio della risposta
                try {
                    linea1 = LineUtility.getLine(nomeFile, numLinea1);
                    linea2 = LineUtility.getLine(nomeFile, numLinea2);
                    BufferedReader br = new BufferedReader(new FileReader(nomeFile));
                    String line;
                    //numLine: tiene conto del numero totale di righe lette
                    int numLine = 0;
                    PrintWriter pw = new PrintWriter(nomeFile);
                    //ciclo che mi stampa ogni riga del file
                    //quando si arriva a una delle righe da scambiare
                    //viene stampata la riga che sostituisce quella precedente

                    //esempio: ho da scambiare la riga 3 con la 5.
                    //quando arrivo alla riga 3 stampo la 5.
                    //quando arrivo alla riga 5 stampo la 3.

                    //il ciclo while finisce o nel momento in cui è finito il file,
                    // oppure quando si sono scambiate entrambe le righe (Ciò avviene quando i
                    // l numero di righe lette è maggiore di entrambi gli indici
                    // delle righe che si devono scambiare)
                    while ((line = br.readLine()) != null && numLine > numLinea1 && numLine > numLinea2) {

                        if (numLine == numLinea1) {
                            pw.print(linea2);
                        } else if (numLine == numLinea2) {
                            pw.print(linea1);
                        }
                        numLine++;

                    }
                } catch (IOException e) {
                    System.err.println("Problemi nell'invio della risposta: "
                            + e.getMessage());
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio
                    // del ciclo
                }
  
                System.out.println("1: Inversione righe avvenuta con successo");
            } // while

        }
        // qui catturo le eccezioni non catturate all'interno del while
        // in seguito alle quali il server termina l'esecuzione
        catch (
                Exception e) {
            e.printStackTrace();
        }
        System.out.println("SwapRow Server: termino...");
        socket.close();
    }
}
