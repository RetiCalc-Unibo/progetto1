import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class RowSwapServer extends Thread {
    //System.out.println("RowSwap server: avviato");

    DatagramSocket socket = null;
    DatagramPacket packet = null;
    byte[] buf = new byte[256];
    int port = -1;
    int success = 1; /* indica l'esito dell'inversione delle righe */
    String fileName = null;

    public RowSwapServer(int port, String file) {
        //NB il controllo del numero della porta viene fatto nel Discovery Server
        this.port = port;
        this.fileName = file;
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
            int numLine1, numLine2 = -1;
            String request = null;
            ByteArrayInputStream biStream = null;
            DataInputStream diStream = null;
            StringTokenizer st = null;
            ByteArrayOutputStream boStream = null;
            DataOutputStream doStream = null;
            String line1, line2 = null;
            byte[] data = null;

            while (true) {
                System.out.println("\nIn attesa di richieste...");
                success = 1;

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
                    request = diStream.readUTF();
                    st = new StringTokenizer(request);

                    numLine1 = Integer.parseInt(st.nextToken());
                    System.out.println("Richiesta linea " + numLine1 + " del file " + fileName);
                    numLine2 = Integer.parseInt(st.nextToken());
                    System.out.println("Richiesta linea " + numLine2 + " del file " + fileName);
                } catch (Exception e) {
                    System.err.println("Problemi nella lettura della richiesta: " + fileName);
                    e.printStackTrace();
                    continue;
                    // il server continua a fornire il servizio ricominciando dall'inizio del ciclo
                }

                // preparazione della linea e invio della risposta
                try {
                    line1 = LineUtility.getLine(fileName, numLine1);
                    line2 = LineUtility.getLine(fileName, numLine2);
                    BufferedReader br = new BufferedReader(new FileReader(fileName));
                    String line = null;
                    int numLine = 1; /* numLine tiene conto del numero totale di righe lette */
                    

                    File ftemp = new File(fileName +".tmp");
                    PrintWriter pw = new PrintWriter(ftemp, "UTF-8");

                    // ciclo che stampa ogni riga del file
                    // quando si arriva a una delle righe da scambiare, viene stampata la riga che sostituisce quella precedente

                    // esempio: si vuole scambiare la riga 3 con la 5.
                    // quando arrivo alla riga 3 stampo la 5, quando arrivo alla 5 stampo la 3.

                    // il ciclo while finisce nel momento in cui è finito il file o quando si sono scambiate entrambe le righe
                    // (ciò avviene quando il numero di righe lette è maggiore di entrambi gli indici delle righe che si devono scambiare)

                    while ((line = br.readLine()) != null ) {
                        if (numLine == numLine1) {
                            pw.println(line2);
                        } else if (numLine == numLine2) {
                            pw.println(line1);
                        } else {
                        	pw.println(line);
                        }
                        numLine++;
                    }
                    
                    br.close();
                    pw.close();
                    ftemp.renameTo(new File(fileName));
                } catch (IOException e) {
                    System.err.println("Problemi nello scambio righe: " + e.getMessage());
                    e.printStackTrace();
                    success = -1;
                }

                try {
                    boStream = new ByteArrayOutputStream();
                    doStream = new DataOutputStream(boStream);
                    doStream.writeUTF(String.valueOf(success));
                    data = boStream.toByteArray();

                    //Riempimento e invio del pacchetto al client:
                    packet.setData(data, 0, data.length);
                    socket.send(packet);
                    System.out.println("RS Server: Inversione righe avvenuta con successo");
                } catch (IOException e) {
                    System.err.println("Problemi nell'invio della risposta: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            }
        }

        // qui catturo le eccezioni non catturate all'interno del while
        // in seguito alle quali il server termina l'esecuzione
        catch (Exception e) {
        	System.out.println("Eccezione non contemplata!\n");
            e.printStackTrace();
        }      

        System.out.println("SwapRow Server: termino...");
        socket.close();
    }
}