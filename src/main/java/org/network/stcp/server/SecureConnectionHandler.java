package org.network.stcp.server;


import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Certificate certificate = CertificateManager.getInstance().getCertificate();
		Key privateKey = CertificateManager.getInstance().getPrivateKey();
		byte[] certBytes;
		try {
			certBytes = certificate.getEncoded();
			//send the certificate
			os.write(certBytes);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			while (is.read(buffer) != -1) {
				baos.write(buffer);
			}
			byte[] keyPacketEncrypted = baos.toByteArray();
			Cipher privateCipher = Cipher.getInstance("DSA");
			privateCipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] keyPacketDecrypted = privateCipher.doFinal(keyPacketEncrypted);

			SecretKey secretKey = new SecretKeySpec(keyPacketDecrypted, "DSA");
			Cipher communicationCipher = Cipher.getInstance("DSA");
			communicationCipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] dataBuffer = new byte[1024 * 4];
			while (is.read(dataBuffer) != -1) {
				byte[] encryptedData = communicationCipher.doFinal(dataBuffer);
				handler.handle(encryptedData);
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
