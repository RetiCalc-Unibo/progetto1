import java.io.*;
import java.net.*;

public class RSClient{

	public static void main(String[] args){



		InetAddress addr = null;
		int port = -1;

		// settaggio argomenti 
		try{
			if(args.length == 3){
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			} else {
				System.out.println("Usage: java RSClient IPDiscoveryServer portDS fileName");
				System.exit(1);
			}
		} catch (UnknownHostException e){
			System.out.println("Problemi nella determinazione dell'endpoint del server : ");
			e.printStackTrace();
			System.out.println("RSClient: interrompo...");
			System.exit(2);
		}

		// realizzazione socket e settaggio timeout 30s
		// creazione datagram packet

		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];

		try{
			socket = new DatagramSocket();
			socket.setSoTimeout(30000);
			packet = new DatagramPacket(buf, buf.length, addr, port);
			System.out.println("\nRSClient: avviato");
			System.out.println("Creata la socket: " + socket);

		} catch (SocketException e){
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.out.println("RSClient: interrompo...");
			System.exit(1);
		}

		// compilazione richiesta al server per porta RS
		
		// output var
		byte [] data = null;
		ByteArrayOutputStream boStream = null;
		DataOutputStream doStream = null;
		
		// input var
		ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;
		String risposta = null;
		int res = -1;
		
		
		try {
			
			boStream = new ByteArrayOutputStream();
			doStream = new DataOutputStream(boStream);
			
			doStream.writeUTF(args[2]);
			data = boStream.toByteArray();
			
			packet.setData(data);
			socket.send(packet);
			
			System.out.println("Richiesta inviata a Discovery Server: " + addr + ", "+ port);
			
		}	catch(IOException e) {
			
			System.out.println("Problemi nell'invio della richiesta: ");
			e.printStackTrace();
			
			System.exit(2);
		}
		
		// settaggio ricezione 
		
		try {			
			packet.setData(buf);
			socket.receive(packet);
			// attende al piu 30s poiche timeout settato, dopodiche 
			// solleva SocketException (compresa nel IOException)
			
		} catch(IOException e) {
			
			System.out.println("Problemi nella ricezione del datagramma: ");
			e.printStackTrace();
			System.exit(3);
		}
		
		try {
			biStream = new ByteArrayInputStream(packet.getData(), 0 , packet.getLength());
			diStream = new DataInputStream(biStream);
			risposta = diStream.readUTF();
			
			res = Integer.valueOf(risposta);
			
		} catch (IOException e) {
			System.out.println("Problema lettura risposta: ");
			e.printStackTrace();
			System.exit(4);
		}
		// ricompilo packet con porta RS
		if(res != -1)
			packet.setPort(res);
		
		else {
			System.out.println("DiscoveryServer: file non presente");
			System.exit(5);
		}
		
		// inizio ciclo richieste a Row Swap
		String insert = null;
		int row1 = -1; 
		int row2 = -1;
		BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));

		try{ 

			System.out.println("Inserire prima riga da invertire");

			while((insert = stdIn.readLine())) != null || row1 == -1) {
					
				boStream.reset();
				row1 = -1;
				row2 = -1; 	
													
				try { 
					row1 = Integer.parseInt(insert);
					
				}catch(NumberFormatException e) {
					
					System.out.println("Problema interazione da console: ");
					e.printStackTrace();
					System.out
					.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserire numero riga 1: ");
					continue;
				}
				
				System.out.println("Inserire seconda riga con cui invertire");

				while((insert = stdIn.readLine())) != null || row2 == -1) { 
					
					try { 
						row2 = Integer.parseInt(insert);
						
					} catch(NumberFormatException e) {
						
						System.out.println("Problema interazione da console: ");
						e.printStackTrace();
						System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserire numero riga 2: ");
						continue;
					}
				}

				// compilo richiesta per RS server 
				try {
					boStream.reset();
					doStream.writeUTF(row1 + " " + row2);
					data = boStream.toByteArray();
					packet.setData(data, 0 , data.length);
					socket.send(packet);
					System.out.println("Richiesta inviata correttamente al RowSwap");

				}catch(IOException e) {
			
					System.out.println("Problemi nell'invio della richiesta a RowSwap: ");
					e.printStackTrace();
					
					System.exit(6);
				}

				try {			
					packet.setData(buf);
					socket.receive(packet);
					// attende al piu 30s poiche timeout settato, dopodiche 
					// solleva SocketException (compresa nel IOException)
					
				} catch(IOException e) {
					
					System.out.println("Problemi nella ricezione del datagramma: ");
					e.printStackTrace();
					System.exit(7);
				}
				
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0 , packet.getLength());
					diStream = new DataInputStream(biStream);
					risposta = diStream.readUTF();
					
					res = Integer.valueOf(risposta);
					
				} catch (IOException e) {
					System.out.println("Problema lettura risposta: ");
					e.printStackTrace();
					System.exit(8);
				}

				// controllo esito operazione 
				if(res != -1){
					System.out
						.println("Operazione svolta con successo");
						ystem.out
			    	System.out
			    		.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserire prima riga da invertire: ");
				}
				
				else {
					System.out.println("Row Server: operazione non eseguita correttamente");
					System.exit(9);
				}
			}

		} catch (Exception e){

			System.out.println("Eccezione non prevista: ");
			e.printStackTrace();

		}

		System.out.println("RSClient: terminazione..");

		socket.close();

	}
}