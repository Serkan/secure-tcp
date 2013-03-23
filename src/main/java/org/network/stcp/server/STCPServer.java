package org.network.stcp.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hello world!
 */
public class STCPServer {

	/*
	*   - Server ilk bağlantıda sertifikasını göndercek
	*   -client sertifikanın geçerli olup olmadığını kontrol edicek
	*   -simetrik key üretip sertifikadaki public key ile key encrypt edip göndericek
	*   -bütün data geçişleri paylaşılan simetirk key ile chiper üzerinden şifrelnip açılacak
	*
	 */


	public void listen(int port, ConnectionHandler dataHandler) throws IOException {
		ServerSocket socket = new ServerSocket(port);
		while (true) {
			Socket client = socket.accept();
			SecureConnectionHandler handler = new SecureConnectionHandler(client, dataHandler);
			Thread thread = new Thread(handler);
			thread.start();
		}

	}

}
