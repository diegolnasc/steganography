package com.d2relz.steganography.cryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * @author Diego
 */
public class AES {

	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES";

	public synchronized void encrypt(String key, File inputFile, File outputFile) throws Exception {
		System.out.println(key);
		doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
	}

	public synchronized void decrypt(String key, File inputFile, File outputFile) throws Exception {
		doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
	}

	private void doCrypto(int cipherMode, String key, File inputFile, File outputFile) throws InvalidKeyException,
			NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException {
		Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(cipherMode, secretKey);
		FileInputStream inputStream = new FileInputStream(inputFile);
		byte[] inputBytes = new byte[(int) inputFile.length()];
		inputStream.read(inputBytes);
		byte[] outputBytes = cipher.doFinal(inputBytes);
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		outputStream.write(outputBytes);
		inputStream.close();
		outputStream.close();
	}
}