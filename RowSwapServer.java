import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class RowSwapServer {

	private String file;
	private int port;

	// Costruttore del RS Server in base alle specifiche prese da DiscoveryServer
	public RowSwapServer(String file, int port){
		this.file = file;
		this.port = port;
		this.idleRowSwapRequest();
	}

	public void idleRowSwapRequest(){
		DatagramSocket socket = null;
		DatagramPacker packet = null;
		byte[] buf = new byte[1000];

		try {
			socket = new DatagramSocket(InetAddress.getLocalHost(), this.port);
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
		} catch (SocketException e){
			e.printStackTrace();
		}
	}
}