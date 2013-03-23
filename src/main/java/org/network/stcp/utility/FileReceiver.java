package org.network.stcp.utility;

import org.network.stcp.server.ConnectionHandler;
import org.network.stcp.server.STCPServer;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: serkan
 * Date: 3/23/13
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileReceiver {


	public static void main(String[] args) throws IOException {
		int port = 7777;
		STCPServer server = new STCPServer();
		server.listen(port, new ConnectionHandler() {
			@Override
			public void handle(byte[] data) {
				System.out.println(new String(data));
			}
		});
	}
}
