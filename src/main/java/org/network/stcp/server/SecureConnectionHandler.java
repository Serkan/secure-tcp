package org.network.stcp.server;


import org.network.stcp.common.STCPConstants;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: serkan
 * Date: 3/23/13
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class SecureConnectionHandler implements Runnable {

	private Socket socket;

	private ConnectionHandler handler;

	public SecureConnectionHandler(Socket socket, ConnectionHandler handler) {
		this.handler = handler;
		this.socket = socket;
	}

	@Override
	public void run() {
		InputStream is;
		OutputStream os;
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
			Certificate certificate = CertificateManager.getInstance().getCertificate();
			Key privateKey = CertificateManager.getInstance().getPrivateKey();
			byte[] certBytes;
			certBytes = certificate.getEncoded();

			int certLength = certBytes.length;
			byte[] certLengthBa = ByteBuffer.allocate(4).putInt(certLength).array();
			byte[] packet = new byte[STCPConstants.TYPE_LENGTH + STCPConstants.INT_LENGTH + certLength];
			//prepare certificate packet
			int index = 0;
			packet[index] = STCPConstants.CERT_PACKET_TYPE;
			index += STCPConstants.TYPE_LENGTH;
			System.arraycopy(certLengthBa, 0, packet, index, STCPConstants.INT_LENGTH);
			index += STCPConstants.INT_LENGTH;
			System.arraycopy(certBytes, 0, packet, index, certLength);
			//send the certificate
			os.write(packet);

			byte[] keyPacketHeader = new byte[STCPConstants.TYPE_LENGTH + STCPConstants.INT_LENGTH];
			is.read(keyPacketHeader);
			index = 0;
			if (keyPacketHeader[index] != STCPConstants.KEY_PACKET_TYPE) {
				throw new IllegalStateException("Expected key packet, instead received differet type packet");
			}
			index += STCPConstants.TYPE_LENGTH;
			byte[] keySizeBa = new byte[STCPConstants.INT_LENGTH];
			System.arraycopy(keyPacketHeader, index, keySizeBa, 0, STCPConstants.INT_LENGTH);
			int keySize = ByteBuffer.wrap(keySizeBa).getInt();
			byte[] encrptedSecretKeyBa = new byte[keySize];
			is.read(encrptedSecretKeyBa);

			Cipher privateCipher = Cipher.getInstance(STCPConstants.CERT_ALGORITHM);
			privateCipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decryptedSecretKey = privateCipher.doFinal(encrptedSecretKeyBa);

			SecretKey secretKey = new SecretKeySpec(decryptedSecretKey, STCPConstants.CIPHER_ALGORITHM);
			Cipher receiveCipher = Cipher.getInstance(STCPConstants.CIPHER_ALGORITHM);
			receiveCipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] dataBuffer = new byte[STCPConstants.SecurePacketSize];
			while (is.read(dataBuffer) != -1) {
				byte[] decryptedData = receiveCipher.doFinal(dataBuffer);
				handler.handle(decryptedData);
			}
		} catch (CertificateEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		}
	}
}
