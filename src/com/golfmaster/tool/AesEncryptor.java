package com.golfmaster.tool;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

/**
 * AES加解密:
 * 
 * 進階加密標準（英語：Advanced Encryption
 * Standard，縮寫：AES），在密碼學中又稱Rijndael加密法，是美國聯邦政府採用的一種區段加密標準。
 * 這個標準用來替代原先的DES，已經被多方分析且廣為全世界所使用。經過五年的甄選流程，進階加密標準由美國國家標準與技術研究院（NIST）於2001年11月26日
 * 發布於FIPS PUB 197，並在2002年5月26日成為有效的標準。 2006年，進階加密標準已然成為對稱金鑰加密中最流行的演算法之一。
 * 該演算法為比利時密碼學家Joan Daemen和Vincent Rijmen所設計，結合兩位作者的名字，
 * 以Rijndael為名投稿進階加密標準的甄選流程。（Rijndael的發音近於"Rhine doll"）
 * 
 * REF:
 * ==>https://zh.wikipedia.org/wiki/%E9%AB%98%E7%BA%A7%E5%8A%A0%E5%AF%86%E6%A0%87%E5%87%86
 * 
 */
public class AesEncryptor {
	private static final String SECRET_KEY = "AesIncrSecretKey"; // 128 bit
																	// key
	private static final String INIT_VECTOR = "AesEncInitVector"; // 16 bytes
																	// IV
	private static final String AES_KEY = "76910E222B531A3787BE2334A4B29B30";
	private static final String AES_IV = "1D59529FDF6A6B5C";

	public static String encrypt(String value) {
		return encrypt(SECRET_KEY, INIT_VECTOR, value);
	}

	public static String decrypt(String encrypted) {
		return decrypt(SECRET_KEY, INIT_VECTOR, encrypted);
	}

	private static String encrypt(String key, String initVector, String value) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));

			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value.getBytes());

			// System.out.println("encrypted string: "
			// + Base64.encodeBase64String(encrypted));

			// return Base64.encodeBase64String(encrypted);
			String s = new String(Base64.getEncoder().encode(encrypted));
			return s;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private static String decrypt(String key, String initVector, String encrypted) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(org.apache.tomcat.util.codec.binary.Base64.decodeBase64(encrypted));

			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}


	private static String decrypt2(String encryptedData) {
		try {
			byte[] keyBytes = AES_KEY.getBytes("UTF-8");
			byte[] ivBytes = AES_IV.getBytes("UTF-8");

			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
			byte[] originalBytes = cipher.doFinal(encryptedBytes);

			return new String(originalBytes, "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	// ***********************************************************************

	/**
	 * Test Example:
	 * 
	 * Note: account=golfController encryptedValue=wU4bTAacqiCiSvZvQmmJew==
	 * decryptedValue=golfController password=golf1234!
	 * encryptedValue=ckv5iGnb2b2wwnPucgguHg== decryptedValue=golf1234!
	 * 
	 * @throws Exception
	 * @throws BadPaddingException
	 * 
	 */
	public static void main(String[] args) {
		String test = "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";
		String account = "golfController";
		String password = "golf1234!";
		String encryptedValue = AesEncryptor.encrypt(account);
		String decryptedValue = AesEncryptor.decrypt(encryptedValue);
		System.out.println("account=" + account);
		System.out.println("encryptedValue=" + encryptedValue);
		System.out.println("decryptedValue=" + decryptedValue);
		encryptedValue = AesEncryptor.encrypt(password);
		decryptedValue = AesEncryptor.decrypt(encryptedValue);
		System.out.println("password=" + password);
		System.out.println("encryptedValue=" + encryptedValue);
		System.out.println("decryptedValue=" + decryptedValue);
		System.out.println("-----");
		String dV = AesEncryptor.decrypt2(test);
		System.out.println("KinMas decryptedValue=" + dV);
	}

}
