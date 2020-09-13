/*******************************************************************************
 * Copyright Julio Leite
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package leitej.crypto;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import leitej.crypto.asymmetric.CipherAsymEnum;
import leitej.crypto.asymmetric.ModeAsymEnum;
import leitej.crypto.asymmetric.PaddingAsymEnum;
import leitej.crypto.asymmetric.signature.SignatureEnum;
import leitej.crypto.hash.HMacEnum;
import leitej.crypto.hash.MessageDigestEnum;
import leitej.crypto.keyStore.KeyStoreEnum;
import leitej.crypto.symmetric.CipherEnum;
import leitej.crypto.symmetric.ModeEnum;
import leitej.crypto.symmetric.PBEEnum;
import leitej.crypto.symmetric.PBEEnum.IterationCountEnum;
import leitej.crypto.symmetric.PaddingEnum;
import leitej.crypto.symmetric.StreamCipherEnum;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.KeyStoreLtException;
import leitej.log.Logger;

/**
 * Cryptography
 *
 * @author Julio Leite
 */
public final class Cryptography {

	private static final Logger LOG = Logger.getInstance();

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final StringBuilder SB_TMP = new StringBuilder();
	private static final Random RANDOM = new Random();

	static {
		final Provider bcProvider = new BouncyCastleProvider();
		Security.addProvider(bcProvider);
		LOG.info("#0", bcProvider);
		providerAvailable();
		if (!hasUnrestrictedPolicyFiles()) {
			LOG.warn("You can find the unrestricted policy files on the same page as the JCE_JDK downloads are found.");
			LOG.warn(
					"Normally it will be a discrete link at the bottom of the download page entitled something like \"Unlimited Strength Jurisdiction Policy Files.\"");
			LOG.warn("The download is a ZIP file, and providing it is legal for you to do so;");
			LOG.warn(
					"You should download the ZIP file and install the two JAR files it contains according to the instructions in the README file contained in the ZIP file.");
		}
	}

	private static void providerAvailable() {
		for (final Provider provider : Security.getProviders()) {
			LOG.debug("#0", provider);
		}
	}

	// just to access the class to run static initialisation
	private static void access() {
	}

	private Cryptography() {
	}

	/*
	 * Policy
	 */

	/**
	 *
	 * @return true if JVM has Unrestricted Policy Files
	 */
	public static boolean hasUnrestrictedPolicyFiles() {
		boolean result = false;
		try {
			result = Cipher.getMaxAllowedKeyLength(
					getTransformation(CipherEnum.Blowfish, ModeEnum.ECB, PaddingEnum.NoPadding)) == Integer.MAX_VALUE;
		} catch (final NoSuchAlgorithmException e) {
			LOG.error("#0", e);
		}
		return result;
	}

	/*
	 * Cipher
	 */

	/**
	 *
	 * @param algorithm
	 * @param mode
	 * @param padding
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 */
	public static Cipher getCipher(final CipherEnum algorithm, final ModeEnum mode, final PaddingEnum padding)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance(getTransformation(algorithm, mode, padding), ProviderEnum.BC.getName());
	}

	/**
	 *
	 * @param algorithm
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 */
	public static Cipher getCipher(final StreamCipherEnum algorithm)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance(algorithm.getName(), ProviderEnum.BC.getName());
	}

	/**
	 *
	 * @param algorithm
	 * @param mode
	 * @param padding
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 */
	public static Cipher getCipher(final CipherAsymEnum algorithm, final ModeAsymEnum mode,
			final PaddingAsymEnum padding)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance(getTransformation(algorithm, mode, padding), ProviderEnum.BC.getName());
	}

	private static String getTransformation(final CipherEnum algorithm, final ModeEnum mode,
			final PaddingEnum padding) {
		synchronized (SB_TMP) {
			SB_TMP.setLength(0);
			return SB_TMP.append(algorithm.getName()).append("/").append(mode.getName()).append("/")
					.append(padding.getName()).toString();
		}
	}

	private static String getTransformation(final CipherAsymEnum algorithm, final ModeAsymEnum mode,
			final PaddingAsymEnum padding) {
		synchronized (SB_TMP) {
			SB_TMP.setLength(0);
			return SB_TMP.append(algorithm.getName()).append("/").append(mode.getName()).append("/")
					.append(padding.getName()).toString();
		}
	}

	/**
	 *
	 * @param algorithm
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 */
	public static Cipher getCipher(final PBEEnum algorithm)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance(algorithm.getName(), ProviderEnum.BC.getName());
	}

	/*
	 * Key
	 */

	public static SecretKey keyProduce(final CipherEnum algorithm, final byte[] keyBytes)
			throws IllegalArgumentLtRtException {
		if (algorithm == null || keyBytes == null) {
			throw new IllegalArgumentLtRtException();
		}
		return new SecretKeySpec(keyBytes, algorithm.getName());
	}

	public static SecretKey keyGenerate(final CipherEnum algorithm, final int bitkeysize)
			throws IllegalArgumentLtRtException, NoSuchAlgorithmException, NoSuchProviderException {
		if (algorithm == null || bitkeysize < 1) {
			throw new IllegalArgumentLtRtException();
		}
		final KeyGenerator generator = KeyGenerator.getInstance(algorithm.getName(), ProviderEnum.BC.getName());
		generator.init(bitkeysize);
		return generator.generateKey();
	}

	/*
	 * IV
	 */

	public static IvParameterSpec ivProduce(final byte[] ivBytes) throws IllegalArgumentLtRtException {
		if (ivBytes == null) {
			throw new IllegalArgumentLtRtException();
		}
		return new IvParameterSpec(ivBytes);
	}

	public static IvParameterSpec ivGenerate(final int bitsize) throws IllegalArgumentLtRtException {
		if (bitsize < 1) {
			throw new IllegalArgumentLtRtException();
		}
		final byte[] ivBytes = new byte[(bitsize + 7) / 8];
		synchronized (SECURE_RANDOM) {
			SECURE_RANDOM.nextBytes(ivBytes);
		}
		return new IvParameterSpec(ivBytes);
	}

	/*
	 * PBE
	 */

	public static SecretKey pbeKeyProduce(final PBEEnum algorithm, final char[] password, final byte[] salt,
			final IterationCountEnum iterationCount) throws IllegalArgumentLtRtException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException {
		if (iterationCount == null) {
			throw new IllegalArgumentLtRtException();
		}
		return pbeKeyProduce(algorithm, password, salt, iterationCount.iterationCount());
	}

	public static SecretKey pbeKeyProduce(final PBEEnum algorithm, final char[] password, final byte[] salt,
			final int iterationCount) throws IllegalArgumentLtRtException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException {
		if (algorithm == null || password == null || salt == null || iterationCount < 1) {
			throw new IllegalArgumentLtRtException();
		}
		final PBEKeySpec pbeSpec = new PBEKeySpec(password, salt, iterationCount);
		final SecretKeyFactory keyFact = SecretKeyFactory.getInstance(algorithm.getName(), ProviderEnum.BC.getName());
		return keyFact.generateSecret(pbeSpec);
	}

	public static byte[] saltGenerate(final PBEEnum algorithm) {
		final byte[] result = new byte[algorithm.goodSaltByteSize()];
		synchronized (RANDOM) {
			RANDOM.nextBytes(result);
		}
		return result;
	}

	/*
	 * MessageDigest
	 */

	public static MessageDigest getMessageDigest(final MessageDigestEnum digestAlgorithm)
			throws NoSuchAlgorithmException, NoSuchProviderException {
		return MessageDigest.getInstance(digestAlgorithm.getName(), ProviderEnum.BC.getName());
	}

	/*
	 * HMAC
	 */

	public static Mac getHmac(final HMacEnum hmacAlgorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
		return Mac.getInstance(hmacAlgorithm.getName(), ProviderEnum.BC.getName());
	}

	public static SecretKey hmacKeyProduce(final HMacEnum hmacAlgorithm, final byte[] hMacKeyBytes)
			throws IllegalArgumentLtRtException {
		if (hmacAlgorithm == null || hMacKeyBytes == null) {
			throw new IllegalArgumentLtRtException();
		}
		return new SecretKeySpec(hMacKeyBytes, hmacAlgorithm.getName());
	}

	public static SecretKey hmacKeyGenerate(final HMacEnum hmacAlgorithm, final int bitkeysize)
			throws IllegalArgumentLtRtException, NoSuchAlgorithmException, NoSuchProviderException {
		if (hmacAlgorithm == null || bitkeysize < 1) {
			throw new IllegalArgumentLtRtException();
		}
		final KeyGenerator generator = KeyGenerator.getInstance(hmacAlgorithm.getName(), ProviderEnum.BC.getName());
		generator.init(bitkeysize);
		return generator.generateKey();
	}

	/*
	 * Signature
	 */

	public static Signature getSignature(final SignatureEnum signatureAlgorithm)
			throws NoSuchAlgorithmException, NoSuchProviderException {
		return Signature.getInstance(signatureAlgorithm.getName(), ProviderEnum.BC.getName());
	}

	/*
	 * RSA
	 */

	public static final class RSA {

		static {
			Cryptography.access();
		}

		public static KeyPair keyPairProduce(final BigInteger modulus, final BigInteger publicExponent,
				final BigInteger privateExponent) throws NoSuchAlgorithmException, NoSuchProviderException {
			final KeyFactory keyFactory = KeyFactory.getInstance(CipherAsymEnum.RSA.getName(),
					ProviderEnum.BC.getName());
			final RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
			final RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(modulus, privateExponent);
			try {
				return new KeyPair(keyFactory.generatePublic(pubKeySpec), keyFactory.generatePrivate(privKeySpec));
			} catch (final InvalidKeySpecException e) {
				throw new ImplementationLtRtException(e);
			}
		}

		/**
		 * When generating RSA key pairs, you normally try to choose a value for the
		 * public exponent and allow the private exponent to be derived from that
		 * accordingly. The JCA allows you to specify both the key size you want and the
		 * public exponent for RSA.<br/>
		 * <br/>
		 * For example, if you wanted to specify one of the standard public exponents
		 * such as the value F4, recommended in X.509, and the default for the Bouncy
		 * Castle provider, you passes the argument <code>publicExponent</code> with the
		 * value java.security.spec.RSAKeyGenParameterSpec.F4
		 *
		 * @param bitkeysize
		 * @param publicExponent
		 * @return
		 * @throws IllegalArgumentLtRtException
		 * @throws NoSuchAlgorithmException
		 * @throws NoSuchProviderException
		 */
		public static KeyPair keyPairGenerate(final int bitkeysize, final BigInteger publicExponent)
				throws IllegalArgumentLtRtException, NoSuchAlgorithmException, NoSuchProviderException {
			if (bitkeysize < 1) {
				throw new IllegalArgumentLtRtException();
			}
			synchronized (SECURE_RANDOM) {
				final KeyPairGenerator generator = KeyPairGenerator.getInstance(CipherAsymEnum.RSA.getName(),
						ProviderEnum.BC.getName());
				try {
					generator.initialize(new RSAKeyGenParameterSpec(bitkeysize, publicExponent), SECURE_RANDOM);
				} catch (final InvalidAlgorithmParameterException e) {
					throw new ImplementationLtRtException(e);
				}
				return generator.generateKeyPair();
			}
		}

		public static KeyPair keyPairGenerate(final int bitkeysize)
				throws IllegalArgumentLtRtException, NoSuchAlgorithmException, NoSuchProviderException {
			if (bitkeysize < 1) {
				throw new IllegalArgumentLtRtException();
			}
			final KeyPairGenerator generator = KeyPairGenerator.getInstance(CipherAsymEnum.RSA.getName(),
					ProviderEnum.BC.getName());
			generator.initialize(bitkeysize);
			return generator.generateKeyPair();
		}

	}

	/*
	 * ElGamal
	 */

	public static final class ElGamal {

		static {
			Cryptography.access();
		}

		public static KeyPair keyPairGenerate(final int bitkeysize)
				throws IllegalArgumentLtRtException, NoSuchAlgorithmException, NoSuchProviderException {
			if (bitkeysize < 1) {
				throw new IllegalArgumentLtRtException();
			}
			final KeyPairGenerator generator = KeyPairGenerator.getInstance(CipherAsymEnum.ElGamal.getName(),
					ProviderEnum.BC.getName());
			generator.initialize(bitkeysize);
			return generator.generateKeyPair();
		}

	}

	/*
	 * KeyStorage
	 */

	/**
	 *
	 * @param keyStoreEnum
	 * @param providerEnum
	 * @return
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException if a KeyStoreSpi
	 *                             implementation for the specified type is not
	 *                             available from the specified provider <br/>
	 *                             +Cause NoSuchProviderException if the specified
	 *                             provider is not registered in the security
	 *                             provider list
	 */
	public static KeyStore getKeyStore(final KeyStoreEnum keyStoreEnum, final ProviderEnum providerEnum)
			throws KeyStoreLtException {
		try {
			return KeyStore.getInstance(keyStoreEnum.getName(), providerEnum.getName());
		} catch (final KeyStoreException e) {
			throw new KeyStoreLtException(e);
		} catch (final NoSuchProviderException e) {
			throw new KeyStoreLtException(e);
		}
	}

}
