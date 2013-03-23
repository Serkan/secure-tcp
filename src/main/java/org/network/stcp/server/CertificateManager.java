package org.network.stcp.server;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * Created with IntelliJ IDEA.
 * User: serkan
 * Date: 3/21/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CertificateManager {

	private static final String passphrase = "123456";

	private static final String alias = "securetcp";

	private static CertificateManager instance;

	private KeyStore keystore;


	private CertificateManager() {
		try {
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(getClass().getResourceAsStream("/mykeystore.jks"), passphrase.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static CertificateManager getInstance() {
		if (instance == null) {
			instance = new CertificateManager();
		}
		return instance;
	}


	public Certificate getCertificate() {
		Certificate certificate;
		try {
			certificate = keystore.getCertificate(alias);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return certificate;
	}


	public Key getPrivateKey() {
		Key privateKey;
		try {
			privateKey = keystore.getKey(alias, passphrase.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return privateKey;
	}

}
