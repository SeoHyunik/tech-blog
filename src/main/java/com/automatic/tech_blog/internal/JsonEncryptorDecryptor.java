package com.automatic.tech_blog.internal;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;
import io.github.cdimascio.dotenv.Dotenv;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileWriter;

public class JsonEncryptorDecryptor {
  private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final String ENV_DIR_PATH = "env";
  private static final String ENV_FILE_PATH = ENV_DIR_PATH + "/.env";
  private static final String ENV_KEY_NAME = "SECRET_KEY_BASE64";

  public static void main(String[] args) throws Exception {
    // Step 1: Generate and store secret key in .env if it doesn't exist
    ensureEnvDirectoryAndSecretKey();

    // Step 2: Load the secret key from .env file and decode it
    Dotenv dotenv = Dotenv.configure().directory(ENV_DIR_PATH).load();
    String secretKeyBase64 = dotenv.get(ENV_KEY_NAME);
    SecretKey secretKey = decodeSecretKey(secretKeyBase64);

    // Step 3: Load the original JSON file
//    String jsonFilePath = "src/main/resources/etc/credentials.json";
//    File jsonFile = new File(jsonFilePath);
//    String jsonContent = new String(Files.readAllBytes(jsonFile.toPath()));

    // Step 4: Encrypt the JSON content using the secret key
    String encryptedJson = encrypt("", secretKey);
    System.out.println("Encrypted JSON: " + encryptedJson);


    // Step 5: Save the encrypted JSON to a txt file
    String directoryPath = "src/main/resources/encrypted";
    saveEncryptedJsonToFile(encryptedJson, directoryPath);

    // Step 6: Decrypt the encrypted JSON for verification
    String decryptedJson = decrypt(encryptedJson, secretKey);
    System.out.println("Decrypted JSON: " + decryptedJson);
  }

  private static void ensureEnvDirectoryAndSecretKey() throws Exception {
    // Create the 'env' directory if it doesn't exist
    File envDir = new File(ENV_DIR_PATH);
    if (!envDir.exists()) {
      envDir.mkdirs();
    }

    // Create .env file with Secret Key if it doesn't exist
    File envFile = new File(ENV_FILE_PATH);
    if (!envFile.exists()) {
      // Generate a new Secret Key and encode it in Base64
      SecretKey secretKey = generateSecretKey();
      String secretKeyBase64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());

      // Write the Secret Key to the .env file
      try (FileWriter writer = new FileWriter(envFile)) {
        writer.write(ENV_KEY_NAME + "=" + secretKeyBase64 + "\n");
      }
      System.out.println(".env file created with Secret Key at: " + envFile.getAbsolutePath());
    }
  }

  private static SecretKey generateSecretKey() throws Exception {
    SecureRandom secureRandom = new SecureRandom();
    byte[] key = new byte[32]; // 256 bits for AES
    secureRandom.nextBytes(key);
    return new SecretKeySpec(key, "AES");
  }

  private static SecretKey decodeSecretKey(String base64Key) {
    byte[] decodedKey = Base64.getDecoder().decode(base64Key);
    return new SecretKeySpec(decodedKey, "AES");
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

  private static void saveEncryptedJsonToFile(String encryptedJson, String directoryPath) throws Exception {
    File directory = new File(directoryPath);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    File file = new File(directory, "openai_api_key.txt");
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(encryptedJson.getBytes());
    }
    System.out.println("Encrypted JSON saved to: " + file.getAbsolutePath());
  }
}

