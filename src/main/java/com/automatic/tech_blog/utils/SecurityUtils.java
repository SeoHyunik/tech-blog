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
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static OAuthCredentials decryptCredentials(String encryptedFilePath) throws Exception {
    // 1. Load and decode the secret key from the environment
    SecretKey secretKey = loadSecretKeyFromEnv();

    // 2. Read the encrypted data from the file
    File encryptedFile = new File(encryptedFilePath);
    String encryptedData = new String(Files.readAllBytes(encryptedFile.toPath()));

    // 3. Decrypt the data
    String decryptedJson = decrypt(encryptedData, secretKey);

    // 4. Convert JSON to OAuthCredentials DTO
    return objectMapper.readValue(decryptedJson, OAuthCredentials.class);
  }

  public static String decryptAuthFile(String encryptedFilePath) throws Exception {
    // 1. Load and decode the secret key from the environment
    SecretKey secretKey = loadSecretKeyFromEnv();

    // 2. Read the encrypted data from the file
    File encryptedFile = new File(encryptedFilePath);
    String encryptedData = new String(Files.readAllBytes(encryptedFile.toPath()));

    // 3. Decrypt the data
    return decrypt(encryptedData, secretKey);
  }

  private static SecretKey loadSecretKeyFromEnv() {
    // 1. Load the secret key from the environment
    Dotenv dotenv = Dotenv.configure().directory(SecuritySpecs.ENV_DIR_PATH.getValue()).load();

    // 2. Decode the secret key from Base64
    String secretKeyBase64 = dotenv.get(SecuritySpecs.ENV_KEY_NAME.getValue());

    // 3. Return the SecretKey object
    byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
    return new SecretKeySpec(decodedKey, SecuritySpecs.SECRET_KEY_ALGORITHM.getValue());
  }

  private static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
    // 1. Split the encrypted data into IV and encrypted bytes
    String[] parts = encryptedData.split(":");
    byte[] iv = Base64.getDecoder().decode(parts[0]);
    byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

    // 2. Decrypt the data
    Cipher cipher = Cipher.getInstance(SecuritySpecs.ALGORITHM.getValue());
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

    // 3. Return the decrypted data as a string
    byte[] decrypted = cipher.doFinal(encryptedBytes);
    return new String(decrypted);
  }
}
