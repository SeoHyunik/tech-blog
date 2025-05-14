package etc_test;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

@Slf4j
public class DecryptPw {

  public static void main(String[] args) {
    log.info("Start DecryptPw");
        String pem =
        """  
            -----BEGIN RSA PRIVATE KEY-----  
            MIIEogIBAAKCAQEAnYewp3WhUmnBwxgmf0JJbpS/GhrJsLRUpL3HTnwx9F24qVt7  
            gI5+lpxCRR9IarZLSbIOxjnLaX4BFxJRDQSv9yV6BfWUoZu8+Pyq9o0vw/NTXmPY  
            Vthd+hrQafOKC/nFAWX8WIfrZrUbATz9CNl9u6UJ54T10l79FRHXI++NDbdo15Vv  
            K1YzT6V67Ij5pGynFXy2Akyv/Xny8J9+HVAu+umOlEgFjQ35F2ivIpXNj6Rj5Xa3  
            w0FncbYNsn2i5NbgPaFh+sGbi0H7ddSAZ0TBXsPdnBQdUnfC8PJqG43e3n9ro4Jo  
            qiJftZrsrNd+HK/b4aoCIQj/3DAzMacPssK8uQIDAQABAoIBAE5+nnfTFV+LcxFT  
            p1siHJUNaWwIjRePw92S8w0YtYueZ7V78D5OwqUpl5v5yovh6gH+x33ero0STFjC  
            t8BU5ZmxAuyVQkqE39Txkf7ouzRYuuH9vn8HsUw+sDsbL15oRtI7j1JN5+51r88e  
            JLeUKCKoShcjGZqJ2nH1Qc6WPyLlyR0QBlXwGsOYYee9VpjHj4Fm5seVvTPnyW51  
            NtOok4bfWhDq9JuJEsgeZZOrxZLpvxwuHm8UM34iGL9Eyeh/aE9lKemHT/lZo8Gh  
            GpKPx63FAev80I4k6U0l+4fmFSleWG/9EwOacwVjJBN6VBK4h8RY60VOznQGPjq2  
            ILzG+AECgYEAzLi2XXYKP8mYLbV2M897cZQOXh2bAWZw6TqhMuWnaJ/7kNwiK4Ib  
            JLm6MOdwaY9acdUGCUS8vFA9tsl3BmRn0RwscRn2f+zMBC0+7/pcvlfXRFnON/3D  
            VLtDsAUxds6DL1nyoNs/AclauL2zmc3Abn/+9Q1dYdNkRPm2bOvRCfECgYEAxPzv  
            GO+yK4+vcBVWxKetSYEBJ/3OnlWFvCWisH6H+0pZuhxzkv/Nt0HTw3OUElt7GQat  
            rqRAakQLadbbpIznVsKG7gDkvwwitDdcxlS+Ke3dTH0b54Cv+20grG6mbrbnRlWv  
            XXB10NEcIXG5V/oL8VtkL9WhLWCPVK+ws/AmV0kCgYBAO9ikfIv8tbE07lHXMcum  
            uHDFoP5osw7MbyPzB1G+pyvUqO1jv1/q5wd/nq6LwHn9a3yIXfPmFjs25gGdlNVq  
            LC/ZkO7h9peQ2+16eJCu/HrDrHXi5ZFrPZKgYRDDGUraCZvyUrhzRA1eF8+Je6Bc  
            S3bgxr+9GQnGBp/xjxA40QKBgAq5eorxAdI9UF/ZyY+LUXPfAiEbQFR+c3l60xCo  
            0t5rfdcUFXa7VofTnaPWdwlI0brbEjmf16Hxcm2gtPSQd0fR72alxc5g6sLFANgL  
            ZQ8DVDkF1q8T4oDdwBmjLIx6iLzbjWCY7tEfkViAyIZxppTNVPcFunCuAR+rMQOa  
            33uBAoGAQBx+qebaolfZUEIE7slK2tMkbsux9oGlyB+LRyZJMo3qiDoCS5pVAEil  
            wv1ICEYxvzDGo1bL5nuhOKTMjvhKwjvQ4oU5hATql6KbINWrj6f6XMcx7IlH1rXF  
            7JsYmgjY4r2KWLdzsO1JWLYraWVhqcz9UI9234Kpqtve9ckfm6M=  
            -----END RSA PRIVATE KEY-----""";
    String encryptedPassword = "mcAzqNlVySh6013oK0Ux5zy0ctbhEsUISHa3kik1a7cRGpW6yly7zx/Ack9f9z98leiTE4KiptUwJtukUAyr8r4TvqZo7a4fNy7uq/BvSzTP2V5I6KUAduwkfPPmA4tbPHxe+leNjgyo71dNqkc8/9wt5+5E5xb0FFUp7vp4Kz7aW8yLMgC4rec8+WYFdYdL5n46+tslhl0H9ueaCgIdus7qCZNMUhjTJokFaNJASRyVrDvrrNB9XsBXajg4B5QIFGtaCv/qlkGy6iA0MCIQI1z5ojRkH/qVNgZhpruN/I37Rau1fLnOPe/aw6KZpmcARbpSmgeT29omeEZ1lYpPdA==";
    DecryptPw decryptPw = new DecryptPw();
    String decryptedPassword = decryptPw.decryptServerPassword(pem, encryptedPassword);
    log.info("Decrypted Password: {}", decryptedPassword);
  }

  public String decryptServerPassword(String pem, String encryptedPassword) {
    log.info("Start decryptServerPassword");
    try {
      log.info("Parsing PEM key...");
      PrivateKey privateKey = parsePEMKeyWithBouncyCastle(pem);
      log.info("PEM key successfully parsed.");

      log.info("Decrypting password...");
      String decryptedText = decryptPasswordWithFallback(privateKey, encryptedPassword);
      log.info("Password successfully decrypted: {}", decryptedText);

      return decryptedText;
    } catch (Exception e) {
      log.error("Error during decryption: {}", e.getMessage(), e);
      return "Decryption failed";
    }
  }

  private PrivateKey parsePEMKeyWithBouncyCastle(String pem) throws Exception {
    log.info("Start parsePEMKeyWithBouncyCastle");

    try (PEMParser pemParser = new PEMParser(new StringReader(pem))) {
      Object object = pemParser.readObject();

      if (object == null) {
        log.error("PEMParser returned null. Possible corruption in the PEM data.");
        throw new IllegalArgumentException("Invalid or empty PEM key.");
      }

      log.info("PEMParser readObject result: {}", object.getClass().getName());

      JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());

      if (object instanceof PEMKeyPair) {
        log.info("Detected PKCS#1 (RSA PRIVATE KEY) format.");
        KeyPair keyPair = converter.getKeyPair((PEMKeyPair) object);
        log.info("PKCS#1 key successfully converted to KeyPair.");
        return keyPair.getPrivate();
      } else if (object instanceof PrivateKeyInfo) {
        log.info("Detected PKCS#8 (PRIVATE KEY) format.");
        PrivateKey privateKey = converter.getPrivateKey((PrivateKeyInfo) object);
        log.info("PKCS#8 key successfully converted to PrivateKey.");
        return privateKey;
      } else {
        log.error("Unsupported PEM key format: {}", object.getClass().getName());
        throw new IllegalArgumentException("Unsupported or corrupted PEM key format");
      }
    } catch (Exception e) {
      log.error("Exception while parsing PEM key: {}", e.getMessage(), e);
      throw e;
    }
  }


  private String decryptPasswordWithFallback(PrivateKey privateKey, String encryptedPassword) throws Exception {
    log.info("Start decryptPasswordWithFallback");
    byte[] byteEncrypted = Base64.getDecoder().decode(encryptedPassword);
    log.info("Decoded byte array length: {}", byteEncrypted.length);

    if (byteEncrypted.length != 256) {
      log.error("Invalid encrypted data length: {}", byteEncrypted.length);
      throw new IllegalArgumentException("Invalid encrypted data length: " + byteEncrypted.length);
    }

    try {
      return decryptPassword(privateKey, byteEncrypted, "RSA/ECB/PKCS1Padding");
    } catch (BadPaddingException e) {
      log.warn("PKCS1Padding failed. Trying RSA/ECB/NOPADDING as fallback...");
      String decryptedText = decryptPassword(privateKey, byteEncrypted, "RSA/ECB/NOPADDING");
      decryptedText = removeManualPadding(decryptedText);  // 수동 패딩 제거
      return extractLast20Bytes(decryptedText);  // 마지막 20바이트 추출
    }
  }

  private String extractLast20Bytes(String decryptedText) {
    log.info("Start extracting last 20 bytes from decrypted data");
    if (decryptedText.length() >= 20) {
      String last20Bytes = decryptedText.substring(decryptedText.length() - 20);
      log.info("Extracted last 20 bytes: {}", last20Bytes);
      return extractValidText(last20Bytes);
    } else {
      log.warn("Decrypted text is shorter than 20 bytes");
      return extractValidText(decryptedText);  // 전체 텍스트에서 유효한 부분만 반환
    }
  }

  private String removeManualPadding(String decryptedText) {
    int nullCharIndex = decryptedText.indexOf('\0');
    if (nullCharIndex != -1) {
      decryptedText = decryptedText.substring(0, nullCharIndex);
    }
    log.info("Decrypted text after manual padding removal: {}", decryptedText);
    return decryptedText;
  }

  private String extractValidText(String decryptedText) {
    log.info("Start extracting valid text from decrypted data");
    StringBuilder validText = new StringBuilder();
    for (char ch : decryptedText.toCharArray()) {
      if (ch >= 32 && ch <= 126) {  // 프린터블 ASCII 범위만 허용
        validText.append(ch);
      }
    }
    log.info("Extracted valid text: {}", validText.toString().trim());
    return validText.toString().trim();
  }

  private String decryptPassword(PrivateKey privateKey, byte[] byteEncrypted, String padding) throws Exception {
    Cipher cipher = Cipher.getInstance(padding);
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] decryptedByte = cipher.doFinal(byteEncrypted);
    log.info("Decrypted byte array (hex): {}", bytesToHex(decryptedByte));
    return new String(decryptedByte, StandardCharsets.UTF_8);
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x ", b));
    }
    return sb.toString().trim();
  }
}
