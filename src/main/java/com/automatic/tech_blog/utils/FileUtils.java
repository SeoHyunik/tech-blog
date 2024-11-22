package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.enums.InternalPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileUtils {
    public static Set<String> getExistingHtmlFiles() {
      try {
        // 1. Ensure the directory exists
        Path outputDir = Paths.get(InternalPaths.HTML_SAVE_DIR.getPath());
        if (!Files.exists(outputDir))
          Files.createDirectories(outputDir); // Blocking call

        // 2. Scan the directory for existing HTML files and return the set of file names
        try (Stream<Path> stream = Files.list(outputDir)) {
          return stream.filter(path -> path.toString().endsWith(".html"))
              .map(path -> path.getFileName().toString())
              .collect(Collectors.toSet());
        }
      } catch (IOException e) {
        throw new IllegalStateException("Failed to scan existing HTML files", e);
      }
    }

  public static String getHtmlContent(String fileName) {
    try {
      Path filePath = Paths.get(InternalPaths.HTML_SAVE_DIR.getPath(), fileName);

      // Read the file content as bytes and convert to String using UTF-8
      byte[] fileBytes = Files.readAllBytes(filePath);
      return new String(fileBytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read HTML file: " + fileName, e);
    }
  }
}
