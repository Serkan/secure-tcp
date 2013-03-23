package org.network.stcp.client;

import org.network.stcp.common.STCPConstants;

import javax.crypto.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * Created with IntelliJ IDEA.
 * User: serkan
 * Date: 3/23/13
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class STCPClient {

	private Socket socket;

	private Cipher sendCipher;

	private Cipher receiveCipher;

	public STCPClient(InetAddress address, int port) throws IOException {
		this.socket = new Socket(address, port);
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		byte[] packetHeader = new byte[STCPConstants.TYPE_LENGTH + STCPConstants.INT_LENGTH];
		//read the header verify the packet type and determine the certificate size
		is.read(packetHeader);
		if (packetHeader[0] != STCPConstants.CERT_PACKET_TYPE) {
			throw new IllegalStateException("Expected packet is the certificate packet but received different packet");
		}
		//parse certificate size
		byte[] certSizeBa = new byte[STCPConstants.INT_LENGTH];
		is.read(certSizeBa);
		int certSize = ByteBuffer.wrap(certSizeBa).getInt();
		//get certificate as byte array
		byte[] certBa = new byte[certSize];
		is.read(certBa);
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance(STCPConstants.CERT_SPEC);
			InputStream cis = new ByteArrayInputStream(certBa);
			Certificate certificate = certFactory.generateCertificate(cis);
			validate(certificate);
			PublicKey publicKey = certificate.getPublicKey();

			//generate a symetric key and encrypt it with public key of server, then send it to server
			Cipher publicCipher = Cipher.getInstance(STCPConstants.CERT_ALGORITHM);
			publicCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			KeyGenerator keyGenerator = KeyGenerator.getInstance(STCPConstants.CIPHER_ALGORITHM);
			SecretKey secretKey = keyGenerator.generateKey();
			//init cipher which wil be used for data encryption
			initSendCipher(secretKey);
			//init cipher which wil be used for data decryption
			initReceiveCipher(secretKey);

			byte[] secretKeyRaw = secretKey.getEncoded();
			byte[] secretKeyEncrypted = publicCipher.doFinal(secretKeyRaw);
			int keySize = secretKeyEncrypted.length;
			byte[] secretKeySizeBa = ByteBuffer.allocate(4).putInt(keySize).array();
			//prepare key packet
			byte[] keyPacket = new byte[STCPConstants.TYPE_LENGTH + STCPConstants.INT_LENGTH + secretKeyEncrypted.length];
			int index = 0;
			keyPacket[index] = STCPConstants.KEY_PACKET_TYPE;
			index += STCPConstants.KEY_PACKET_TYPE;
			System.arraycopy(secretKeySizeBa, 0, keyPacket, index, STCPConstants.INT_LENGTH);
			index += STCPConstants.INT_LENGTH;
			System.arraycopy(secretKeyEncrypted, 0, keyPacket, index, keySize);
			//send key packet
			os.write(keyPacket);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

	private void initReceiveCipher(SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		receiveCipher = Cipher.getInstance(STCPConstants.CIPHER_ALGORITHM);
		receiveCipher.init(Cipher.DECRYPT_MODE, secretKey);
	}

	private void initSendCipher(SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		sendCipher = Cipher.getInstance(STCPConstants.CIPHER_ALGORITHM);
		sendCipher.init(Cipher.ENCRYPT_MODE, secretKey);
	}

	private void validate(Certificate certificate) {
		//FIXME not yet implemeneted
	}

	public void sendBytes(byte[] data) throws IOException {
		byte[] encryptedData;
		try {
			encryptedData = sendCipher.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
		socket.getOutputStream().write(encryptedData);
	}

	public int receiveBytes(byte[] data) throws IOException {
		//FIXME
		byte[] temp = new byte[data.length];
		int read = socket.getInputStream().read(temp);
		if (read != -1) {
			try {
				temp = receiveCipher.doFinal(temp);
			} catch (IllegalBlockSizeException e) {
				throw new RuntimeException(e);
			} catch (BadPaddingException e) {
				throw new RuntimeException(e);
			}
		}
		return read;
	}


	public void close() throws IOException {
		socket.close();
	}


}
