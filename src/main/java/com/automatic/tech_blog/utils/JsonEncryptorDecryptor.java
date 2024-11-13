package com.automatic.tech_blog.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

public class JsonEncryptorDecryptor {
  private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final String SECRET_KEY_ALGORITHM = "AES";
  private static final int KEY_SIZE = 256;

  public static void main(String[] args) throws Exception {
    // Load the original JSON file
    String jsonFilePath = "src/main/resources/etc/credentials.json";
    File jsonFile = new File(jsonFilePath);
    String jsonContent = new String(Files.readAllBytes(jsonFile.toPath()));

    // Generate secret key
    SecretKey secretKey = generateSecretKey();

    // Encrypt JSON content
    String encryptedJson = encrypt(jsonContent, secretKey);
    System.out.println("Encrypted JSON: " + encryptedJson);

    // Save encrypted JSON to file
    try (FileOutputStream fos = new FileOutputStream("encrypted_credentials.txt")) {
      fos.write(encryptedJson.getBytes());
    }

    // Decrypt JSON content for use
    String decryptedJson = decrypt(encryptedJson, secretKey);
    System.out.println("Decrypted JSON: " + decryptedJson);
  }

  private static SecretKey generateSecretKey() throws Exception {
    KeyGenerator keyGenerator = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM);
    keyGenerator.init(KEY_SIZE);
    return keyGenerator.generateKey();
  }

  private static String encrypt(String data, SecretKey secretKey) throws Exception {
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    byte[] iv = new byte[16];
    SecureRandom random = new SecureRandom();
    random.nextBytes(iv);
    IvParameterSpec ivSpec = new IvParameterSpec(iv);

    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
    byte[] encrypted = cipher.doFinal(data.getBytes());

    String ivBase64 = Base64.getEncoder().encodeToString(iv);
    String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);

    return ivBase64 + ":" + encryptedBase64;
  }

  private static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
    String[] parts = encryptedData.split(":");
    byte[] iv = Base64.getDecoder().decode(parts[0]);
    byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

    Cipher cipher = Cipher.getInstance(ALGORITHM);
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

    byte[] decrypted = cipher.doFinal(encryptedBytes);
    return new String(decrypted);
  }
}
