package com.automatic.tech_blog.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TargetFolders {
  ALGORITHM("Algorithm"),
  IT_KNOWLEDGE("IT Knowledge"),
  JAVA_SPRING("JAVA-SPRING"),
  NOTES("Notes"),
  ATTACHED_IMAGES("Attached File"); // Attached File is a folder for images

  private final String folderName;

  TargetFolders(String folderName) {
    this.folderName = folderName;
  }

  public String getFolderName() {
    return folderName;
  }

  public static List<String> getFolderNames() {
    return Arrays.stream(TargetFolders.values())
        .map(TargetFolders::getFolderName)
        .collect(Collectors.toList());
  }
}
