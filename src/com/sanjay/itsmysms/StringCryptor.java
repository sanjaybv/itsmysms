package com.sanjay.itsmysms;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.widget.Toast;

public class StringCryptor {

	private static final String CIPHER_ALGORITHM = "AES";
	private static final String RANDOM_GENERATOR_ALGORITHM = "SHA1PRNG";
	private static final int RANDOM_KEY_SIZE = 128;

	// attach identifying sample
	public static String attach(String selfno, String finaldata) {

		String key = "sanju";
		char[] ckey = key.toCharArray();
		int dis = (Integer.parseInt(selfno) % 1000) % 5;

		for (int i = 0; i < 5; i++) {
			ckey[i] += dis;
			finaldata = ckey[i] + finaldata;
		}
		return finaldata;

	}

	// Encrypts string and encode in Base64
	public static String encrypt(String password, String data) throws Exception {
		byte[] secretKey = generateKey(password.getBytes());
		byte[] clear = data.getBytes();

		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey,
				CIPHER_ALGORITHM);
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

		byte[] encrypted = cipher.doFinal(clear);
		String encryptedString = Base64.encodeToString(encrypted,
				Base64.DEFAULT);

		return encryptedString;
	}

	//
	public static int checkfor(String senderno, String mbody) {

		int flag = 1;
		if (mbody.length() < 5) {
			flag = 0;
		} else {
			String cbody = mbody.substring(0, 5);
			int dis = (Integer.parseInt(senderno) % 1000) % 5;

			char[] key = cbody.toCharArray();
			for (int i = 0; i <= 4; i++) {
				key[i] -= dis;
				switch (i) {
				case 0:
					if (key[i] != 'u')
						flag = 0;
					break;
				case 1:
					if (key[i] != 'j')
						flag = 0;
					break;
				case 2:
					if (key[i] != 'n')
						flag = 0;
					break;
				case 3:
					if (key[i] != 'a')
						flag = 0;
					break;
				case 4:
					if (key[i] != 's')
						flag = 0;
					break;
				default:
					break;
				}
			}
		}
		return flag;

	}

	public static String detach(int flag, String body) {
		if (flag == 1) {
			body = body.substring(5, body.length());
		}
		return body;
	}

	// Decrypts string encoded in Base64
	public static String decrypt(String password, String encryptedData)
			throws Exception {
		byte[] secretKey = generateKey(password.getBytes());

		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey,
				CIPHER_ALGORITHM);
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

		byte[] encrypted = Base64.decode(encryptedData, Base64.DEFAULT);
		byte[] decrypted = cipher.doFinal(encrypted);

		return new String(decrypted);
	}

	public static byte[] generateKey(byte[] seed) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(CIPHER_ALGORITHM);
		SecureRandom secureRandom = SecureRandom
				.getInstance(RANDOM_GENERATOR_ALGORITHM);
		secureRandom.setSeed(seed);
		keyGenerator.init(RANDOM_KEY_SIZE, secureRandom);
		SecretKey secretKey = keyGenerator.generateKey();
		return secretKey.getEncoded();
	}
}
