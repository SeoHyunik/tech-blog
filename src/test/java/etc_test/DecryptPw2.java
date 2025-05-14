package etc_test;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

@Slf4j
public class DecryptPw2 {

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
    String decryptedPassword = decryptServerPassword(pem, encryptedPassword);
    log.info("Decrypted Password: {}", decryptedPassword);
  }

  public static String decryptServerPassword(String pem, String encryptedPassword) {
    log.info("Start decryptServerPassword");
    try {
      // PEM 파싱
      log.info("Parsing PEM key...");
      PEMParser pemParser = new PEMParser(new StringReader(pem));
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());
      Object object = pemParser.readObject();
      KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
      PrivateKey privateKey = kp.getPrivate();
      log.info("PEM key successfully parsed.");

      // 암호화된 비밀번호 디코딩
      log.info("Decoding encrypted password...");
      byte[] byteEncrypted = Base64.getDecoder().decode(encryptedPassword);
      log.info("Decoded byte array length: {}", byteEncrypted.length);

      // RSA 복호화
      log.info("Decrypting password using RSA/ECB/NOPADDING...");
      Cipher cipher = Cipher.getInstance("RSA/ECB/NOPADDING");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      byte[] decryptedByte = cipher.doFinal(byteEncrypted);
      log.info("Decrypted byte array length: {}", decryptedByte.length);

      // UTF-8 변환 및 마지막 20바이트 추출
      String decryptText = new String(decryptedByte, StandardCharsets.UTF_8).trim();
      log.info("Decrypted full text: {}", decryptText);
      String plainText = decryptText.substring(decryptText.length() - 20);
      log.info("Extracted last 20 bytes: {}", plainText);

      // 비밀번호 검증
      Pattern passwordPattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
      Matcher checkPlainText = passwordPattern.matcher(plainText);
      if (!checkPlainText.matches()) {
        log.error("Invalid password string: {}", plainText);
        return "Invalid password string";
      }

      return plainText;

    } catch (Exception e) {
      log.error("Error during decryption: {}", e.getMessage(), e);
      return "Decryption failed";
    }
  }
}
