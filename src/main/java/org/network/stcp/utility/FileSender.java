package org.network.stcp.utility;

import org.network.stcp.client.STCPClient;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created with IntelliJ IDEA.
 * User: serkan
 * Date: 3/23/13
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileSender {

	public static void main(String[] args) throws IOException {
		InetAddress address = InetAddress.getByName("127.0.0.1");
		int port = 7777;
		STCPClient client = new STCPClient(address, port);
		client.sendBytes("serkan".getBytes());
		client.close();
	}
}
