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
		
		// inizio ciclo richieste a SawSwap
		
		String input = null;
		int raw1 = -1; 
		int raw2 = -1;
		BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));

		try{ 

			while((input = stdIn.readLine()) != null) {

				try {
					
					boStream.reset();
					raw1 = -1;
					raw2 = -1; 	
								
					while((input = stdIn.readLine()) != null || raw1 != -1) {
						
						System.out.println("Inserire prima riga da invertire");
						
						try { 
							raw1 = Integer.valueOf(input);
							
						}catch(NumberFormatException e) {
							
							System.out.println("Problema interazione da console: ");
							e.printStackTrace();
							System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti numero riga 1: ");
							continue;
						}
					}
					
					while((input = stdIn.readLine()) != null || raw2 != -1) { 
						
						System.out.println("Inserire seconda riga con cui invertire");

						try { 
							raw2 = Integer.valueOf(input);
							
						} catch(NumberFormatException e) {
							
							System.out.println("Problema interazione da console: ");
							e.printStackTrace();
							System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti numero riga 2: ");
							continue;
						}
					}
				} catch (IOException e) {

					System.out.println("Problemi invio richiesta a RawSwap: ");
					e.printStackTrace();

					continue;
				}

				// compilo richiesta per RS server 
				try {
					boStream.reset();
					doStream.writeUTF(raw1 + ";" + raw2);
					data = boStream.toByteArray();
					packet.setData(data, 0 , data.length);
					socket.send(packet);

				}catch(IOException e) {
			
					System.out.println("Problemi nell'invio della richiesta: ");
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
				if(res != -1)
					System.out.println("Operazione svolta con successo");
				
				else {
					System.out.println("Raw Server: operazione non eseguita correttamente");
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