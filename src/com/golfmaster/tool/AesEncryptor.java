package com.golfmaster.tool;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
//import org.apache.commons.codec.binary.Base64;
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
public class AesEncryptor
{
	private static final String	SECRET_KEY	= "AesIncrSecretKey";	// 128 bit
																	// key
	private static final String	INIT_VECTOR	= "AesEncInitVector";	// 16 bytes
																	// IV

	public static String encrypt(String value)
	{
		return encrypt(SECRET_KEY, INIT_VECTOR, value);
	}

	public static String decrypt(String encrypted)
	{
		return decrypt(SECRET_KEY, INIT_VECTOR, encrypted);
	}

	private static String encrypt(String key, String initVector, String value)
	{
		try
		{
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
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	private static String decrypt(String key, String initVector, String encrypted)
	{
		try
		{
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

			return new String(original);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	// ***********************************************************************

	/**
	 * Test Example:
	 * 
	 * Note: account=golfController 
	 * encryptedValue=wU4bTAacqiCiSvZvQmmJew==
	 * decryptedValue=golfController 
	 * password=golf1234!
	 * encryptedValue=ckv5iGnb2b2wwnPucgguHg== 
	 * decryptedValue=golf1234!
	 * 
	 */
	public static void main(String[] args)
	{
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
	}

}
