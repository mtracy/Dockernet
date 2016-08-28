import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Dockernode implements Runnable {

	Socket sock;
	HashMap<URI, Socket> socketmap;
	ServerSocket listener;
	URI uri;
	Thread serverThread;
	boolean run;

	public Dockernode(URI uri) {
		this.run = false;
		this.uri = uri;
		socketmap = new HashMap<URI, Socket>();
		try {
			listener = new ServerSocket(uri.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Socket createSocket(URI other) {
		if (socketmap.containsKey(other))
			return socketmap.get(other);

		try {
			System.out.println("creating a socket from " + uri + " to " + other);
			Socket newsock = new Socket(other.getHost(), other.getPort());
			socketmap.put(other, newsock);
			return newsock;
		} catch (UnknownHostException e) {
			System.out.println("Can't find host.");
			return null;
		} catch (IOException e) {
			System.out.println("Error connecting to host.");
			return null;
		}
	}

	public void say(URI other, String str) throws IOException {
		if (!socketmap.containsKey(other)) {
			System.err.println("There does not exist a connection to the specified URI");
			return;
		}

		Socket s = socketmap.get(other);

		OutputStream out = s.getOutputStream();
		PrintWriter pout = new PrintWriter(out, true);
		pout.println(str);

	}

	public void listen(URI other) throws IOException {
		if (!socketmap.containsKey(other)) {
			System.err.println("There does not exist a connection to the specified URI");
			return;
		}

		Socket s = socketmap.get(other);

		InputStream in = s.getInputStream();
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		String response = bin.readLine();
		System.out.println("Client got: " + response);

	}

	public void startServing() {
		this.run = true;
		serverThread = new Thread(this);
		serverThread.start();
	}

	public void serve() throws IOException {
		Socket client;
		try {
			while (run) {
				client = listener.accept();
				// wait for connection
				InputStream in = client.getInputStream();
				OutputStream out = client.getOutputStream();

				BufferedReader bin = new BufferedReader(new InputStreamReader(in));
				String msg = bin.readLine();
				System.out.println("Server got: " + msg);
				PrintWriter pout = new PrintWriter(out, true);
				pout.println("Goodbye from the server!");
				client.close();
			}

		} catch (IOException e) {
			if(run)
				e.printStackTrace();
		} finally {
			if (!listener.isClosed())
				listener.close();
		}
	}

	@Override
	public void run() {
		try {
			serve();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() throws IOException{
		this.run = false;
		
		listener.close();

		for(Socket s : socketmap.values()){
			s.close();
		}
	}

	public static void main(String[] args) throws URISyntaxException, IOException {
		URI u1 = new URI(args[0]);
		Dockernode n1 = new Dockernode(u1);
		n1.startServing();

		//n1.close();

	}

}
