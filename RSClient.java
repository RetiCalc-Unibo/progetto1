import java.io.*;
import java.net.*;

public class RSClient{

	public static void main(String[] args){
		InetAddress addr = null;
		int port = -1;

		try{
			if(args.length == 3){
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			} else {
				System.out.println("Usage: java RSClient IPDiscoveryServer portDS fileName")
				System.exit(1);
			}
		} catch (UnknownHostException e){
			System.out.println("Problemi nella determinazione dell'endpoint del server : ");
			e.printStackTrace();
			System.out.println("RSClientt: interrompo...");
			System.exit(2);
		}

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
	}
}