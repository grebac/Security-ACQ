package hepl.grebac.acq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

@SpringBootApplication
public class AcqApplication {

	public static void main(String[] args) throws IOException {
		System.setProperty("javax.net.ssl.keyStore", "acq.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "heplPass");

		System.setProperty("javax.net.ssl.trustStore", "acqTruststore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "heplPass");

		System.setProperty("javax.net.debug", "ssl");

		SSLServerSocketFactory sslserversocketfactory
				= (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket sslserversocketForHttpsCommunication
				= (SSLServerSocket) sslserversocketfactory.createServerSocket(7777);

		System.out.println("Waiting for client");

		handleHTTPSPortRequest(sslserversocketForHttpsCommunication);
		}
	private static void handleHTTPSPortRequest(SSLServerSocket sslserversocketForHttpsCommunication) throws IOException {
		while (true) {
			try {
				SSLSocket client = (SSLSocket) sslserversocketForHttpsCommunication.accept();
				System.out.println("Received HTTPS connection from ACS");

				BufferedReader input = GetBufferedReader(client);
				BufferedWriter output = GetBufferedWriter(client);

				System.out.println("The user sent a token: " + input.readLine());

				if(requestToACS())
					output.write("ACK\n");
				else
					output.write("NACK\n");

				output.flush();

				client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean requestToACS() throws IOException {
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket sslSocketForACS = (SSLSocket) sslsocketfactory.createSocket("localhost", 3333);

		var writer = GetBufferedWriter(sslSocketForACS);
		var reader = GetBufferedReader(sslSocketForACS);

		writer.write("This is a Token\n");
		writer.flush();

		var answer = reader.readLine();

		if(answer.equals("ACK")) {
			System.out.println("Token is valid");
			return true;
		}
		else {
			System.out.println("Token is invalid");
			return false;
		}
	}

	private static BufferedWriter GetBufferedWriter(SSLSocket sslsocket) throws IOException {
		OutputStream outputstream = sslsocket.getOutputStream();
		BufferedWriter bufferedwriter = new BufferedWriter(new OutputStreamWriter(outputstream));
		return bufferedwriter;
	}

	private static BufferedReader GetBufferedReader(SSLSocket sslsocket) throws IOException {
		InputStream inputstream = sslsocket.getInputStream();
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
		return bufferedreader;
	}
}

