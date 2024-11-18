package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.service.OAuthCredentials;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class SecurityUtils {
  /**
   * Decrypt the encrypted credentials from the given file and return as OAuthCredentials DTO.
   */
  public static OAuthCredentials decryptCredentials(String encryptedFilePath) throws Exception {
    // Load and decode the secret key from the environment
    SecretKey secretKey = loadSecretKeyFromEnv();

    // Read the encrypted data from the file
    File encryptedFile = new File(encryptedFilePath);
    String encryptedData = new String(Files.readAllBytes(encryptedFile.toPath()));

    // Decrypt the data
    String decryptedJson = decrypt(encryptedData, secretKey);

    // Convert JSON to OAuthCredentials DTO
    return objectMapper.readValue(decryptedJson, OAuthCredentials.class);
  }

  /**
   * Decrypt the encrypted credentials from the given file and return as String
   */
  public static String decryptOpenAiApiKey(String encryptedFilePath) throws Exception {
    // Load and decode the secret key from the environment
    SecretKey secretKey = loadSecretKeyFromEnv();

    // Read the encrypted data from the file
    File encryptedFile = new File(encryptedFilePath);
    String encryptedData = new String(Files.readAllBytes(encryptedFile.toPath()));

    // Decrypt the data
    return decrypt(encryptedData, secretKey);
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Load Secret Key from .env file and decode it.
   */
  private static SecretKey loadSecretKeyFromEnv() {
    Dotenv dotenv = Dotenv.configure().directory(SecuritySpecs.ENV_DIR_PATH.getValue()).load();
    String secretKeyBase64 = dotenv.get(SecuritySpecs.ENV_KEY_NAME.getValue());
    return decodeSecretKey(secretKeyBase64);
  }

  /**
   * Decode Base64-encoded Secret Key to AES SecretKey object.
   */
  private static SecretKey decodeSecretKey(String base64Key) {
    byte[] decodedKey = Base64.getDecoder().decode(base64Key);
    return new SecretKeySpec(decodedKey, SecuritySpecs.SECRET_KEY_ALGORITHM.getValue());
  }

  /**
   * Decrypt the encrypted data using the given SecretKey and return the decrypted JSON string.
   */
  private static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
    String[] parts = encryptedData.split(":");
    byte[] iv = Base64.getDecoder().decode(parts[0]);
    byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

    Cipher cipher = Cipher.getInstance(SecuritySpecs.ALGORITHM.getValue());
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

    byte[] decrypted = cipher.doFinal(encryptedBytes);
    return new String(decrypted);
  }
}
