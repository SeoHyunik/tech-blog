package com.automatic.tech_blog.utils;

public class test {
  public static void main(String[] args) {
    try {
      String decryptedJson = String.valueOf(
          SecurityUtils.decryptCredentials("src/main/resources/encrypted/encrypted_credentials.txt"));
      System.out.println("Decrypted JSON: " + decryptedJson);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
