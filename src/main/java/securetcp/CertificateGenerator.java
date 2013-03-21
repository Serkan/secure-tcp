package securetcp;

import sun.security.x509.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: serkan
 * Date: 3/21/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CertificateGenerator {

	/**
	 * Create a self-signed X.509 Certificate
	 *
	 * @param dn        the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param pair      the KeyPair
	 * @param days      how many days from now the Certificate is valid for
	 * @param algorithm the signing algorithm, eg "SHA1withRSA"
	 */
	X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm)
			throws GeneralSecurityException, IOException {
		PrivateKey privateKey = pair.getPrivate();
		X509CertInfo info = new X509CertInfo();
		Date from = new Date();
		Date to = new Date(from.getTime() + days * 86400000l);
		CertificateValidity interval = new CertificateValidity(from, to);
		BigInteger sn = new BigInteger(64, new SecureRandom());
		X500Name owner = new X500Name(dn);

		info.set(X509CertInfo.VALIDITY, interval);
		info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
		info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
		info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
		info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
		info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
		AlgorithmId alg = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
		info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(alg));

		// Sign the cert to identify the algorithm that's used.
		X509CertImpl cert = new X509CertImpl(info);
		cert.sign(privateKey, algorithm);

		// Update the algorithm, and resign.
		alg = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
		info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, alg);
		cert = new X509CertImpl(info);
		cert.sign(privateKey, algorithm);
		return cert;
	}
}
