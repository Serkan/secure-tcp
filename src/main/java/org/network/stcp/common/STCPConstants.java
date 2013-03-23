package org.network.stcp.common;

/**
 * Created with IntelliJ IDEA.
 * User: serkan
 * Date: 3/23/13
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public final class STCPConstants {

	private STCPConstants() {
	}

	public static final int SecurePacketSize = 4 * 1024;

	public static final int TYPE_LENGTH = 1;

	public static final byte CERT_PACKET_TYPE = 0;

	public static final byte DATA_PACKET_TYPE = 1;

	public static final byte KEY_PACKET_TYPE = 2;

	public static final int INT_LENGTH = 4;

	public static final String CERT_SPEC = "X.509";

	public static final String CERT_ALGORITHM = "RSA";

	public static final String CIPHER_ALGORITHM = "AES";


}
