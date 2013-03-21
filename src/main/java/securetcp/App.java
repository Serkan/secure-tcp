package securetcp;


import javax.crypto.Cipher;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;

/**
 * Hello world!
 */
public class App {

	/*
	*   - Server ilk bağlantıda sertifikasını göndercek
	*   -client sertifikanın geçerli olup olmadığını kontrol edicek
	*   -simetrik key ve session id üretip sertifikadaki public key ile key encrypt edip göndericek
	*   -server session id ve key'i connection olarak tutucak
	*   -bütün data geçişleri paylaşılan simetirk key ile chiper üzerinden şifrelnip açılacak
	*
	 */

	private static final String passphrase = "123456";
	private static final String alias = "securetcp";


	public static void main(String[] args) throws IOException, GeneralSecurityException {
		new App().logic();

	}

	public void logic() throws IOException, GeneralSecurityException {
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(getClass().getResourceAsStream("/mykeystore.jks"), passphrase.toCharArray());

		Key privateKey = keystore.getKey(alias, passphrase.toCharArray());
		Certificate certificate = keystore.getCertificate(alias);
		PublicKey publicKey = certificate.getPublicKey();
		Cipher encrypter = Cipher.getInstance("RSA");
		encrypter.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] enMsg = encrypter.doFinal("serkan".getBytes());

		System.out.println(new String(enMsg));


		Cipher decrypter = Cipher.getInstance("RSA");
		decrypter.init(Cipher.DECRYPT_MODE,privateKey);
		byte[] deMsg = decrypter.doFinal(enMsg);

		System.out.println(new String(deMsg));

//		KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
//		KeyPair keyPair = generator.generateKeyPair();
//		CertificateGenerator certGen = new CertificateGenerator();
//		X509Certificate cert = certGen.generateCertificate("CN=Karen Berge,CN=admin,DC=corp,DC=Fabrikam,DC=COM", keyPair, 365, "DSA");


//		keystore.setCertificateEntry();

//		ServerSocket socket = new ServerSocket(7777);
//		while (true) {
//			Socket client = socket.accept();
//			InputStream inputStream = client.getInputStream();
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			byte[] buffer = new byte[1024];
//			while (inputStream.read(buffer) != -1) {
//				baos.write(buffer);
//			}
//			byte[] result = baos.toByteArray();
//			System.out.println(new String(result));
//		}
	}
}
