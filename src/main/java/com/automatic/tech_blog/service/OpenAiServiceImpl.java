package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.request.OpenAiRequest;
import com.automatic.tech_blog.dto.response.OpenAiResponse;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.enums.InternalPaths;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.automatic.tech_blog.utils.GoogleDriveUtils;
import com.automatic.tech_blog.utils.OpenAiUtils;
import com.automatic.tech_blog.utils.SecurityUtils;
import com.google.api.services.drive.Drive;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {
  private final GoogleDriveUtils googleDriveUtils;
  private final OpenAiUtils openAiUtils;

  @Override
  public List<ProcessedDataList> editTechNotes(MdFileLists mdFileLists, GoogleAuthInfo googleAuthInfo) {
    try {
      // 1. Create the Drive service
      Drive driveService = googleDriveUtils.createDriveService(googleAuthInfo, "kiwijam");

      // 2. Scan the local directory for existing HTML files
      Path outputDir = Paths.get(InternalPaths.HTML_SAVE_DIR.getPath());
      if (!Files.exists(outputDir)) {
        Files.createDirectories(outputDir);
      }

      // 3. Put the existing HTML files in a set for quick lookup
      Set<String> existingHtmlFiles;
      try (Stream<Path> stream = Files.list(outputDir)) {
        existingHtmlFiles = stream.filter(path -> path.toString().endsWith(".html"))
            .map(path -> path.getFileName().toString())
            .collect(Collectors.toSet());
      }

      // 4. Process each Markdown file in the list
      List<ProcessedDataList> processedData = new ArrayList<>();
      for (MdFileInfo mdFileInfo : mdFileLists.mdFileLists()) {
        String htmlFileName = mdFileInfo.fileName().replace(".md", ".html");

        // 5. Skip processing if the HTML file already exists locally
        if (existingHtmlFiles.contains(htmlFileName)) {
          log.info("HTML file already exists. Skipping processing for: {}", htmlFileName);
          continue;
        }

        // 6. Fetch file content from Google Drive
        String fileContent = googleDriveUtils.getFileContent(driveService, mdFileInfo.id(), googleAuthInfo);

        if (fileContent != null) {
          // 7. Transform Markdown to HTML
          processedData.add(transformMarkdownToHtml(fileContent, mdFileInfo.fileName()));
        } else {
          log.warn("File content is null for file ID: {}", mdFileInfo.id());
        }
      }
      return processedData;
    } catch (Exception e) {
      log.error("Error while editing tech notes: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to edit tech notes", e);
    }
  }



  private ProcessedDataList transformMarkdownToHtml(String markdownContent, String fileName) {
    try {
      // 1. Load editor roles
      JsonObject roles = loadEditorRoles();

      // 2. Create the prompt with the updated messages
      String prompt = createPrompt(roles, markdownContent);

      // 3. Get Open AI Api_key
      String apiKey = SecurityUtils.decryptOpenAiApiKey(SecuritySpecs.OPEN_AI_SECRET_KEY_FILE_PATH.getValue());

      // 4. Use OpenAI API to convert Markdown to HTML
      OpenAiRequest openAiRequest = new OpenAiRequest(prompt, apiKey);
      OpenAiResponse openAiResponse = openAiUtils.generateHtmlFromMarkdown(openAiRequest);

      // 5. Save the HTML to a local directory
      saveHtmlToLocal(openAiResponse.content(), fileName);
      log.info("Token Usage: {}", openAiResponse.tokenUsage());
      // token 저장로직 추가

      // 6. Return the processed data
      return new ProcessedDataList(fileName, openAiResponse.content());
    } catch (Exception e) {
      log.error("Error converting Markdown to HTML: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to transform Markdown to HTML", e);
    }
  }

  public String createPrompt(JsonObject roles, String markdownContent) {
    try {
      // 1. Get the messages array from roles
      JsonArray messages = roles.getAsJsonArray("messages");
      if (messages == null)
        throw new IllegalStateException("Missing 'messages' array in roles JSON");

      // 2. Create a new message for the markdown content
      JsonObject newMessage = new JsonObject();
      newMessage.addProperty("role", "user");
      newMessage.addProperty("content", "Markdown Content:\n" + markdownContent);

      // 3. Add the new message to the messages array
      messages.add(newMessage);

      // 4. Update the roles object with the modified messages array (optional if roles is mutable)
      roles.add("messages", messages);

      return roles.toString();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create prompt JSON", e);
    }
  }

  private JsonObject loadEditorRoles() {
    try {
      // 1. Load the JSON file from the given path
      Path path = Paths.get(InternalPaths.EDITOR_ROLES.getPath());
      String jsonString = Files.readString(path);

      // 2. Parse the JSON string to a JsonObject
      return JsonParser.parseString(jsonString).getAsJsonObject();
    } catch (IOException e) {
      log.error("Failed to load editor roles from {}", InternalPaths.EDITOR_ROLES.getPath(), e);
      throw new IllegalStateException("Failed to load editor roles", e);
    } catch (Exception e) {
      log.error("Failed to parse editor roles JSON from {}", InternalPaths.EDITOR_ROLES.getPath(), e);
      throw new IllegalStateException("Invalid JSON format in editor roles", e);
    }
  }

  private void saveHtmlToLocal(String htmlContent, String fileName) {
    try {
      // 1. Ensure the directory exists
      Path outputDir = Paths.get(InternalPaths.HTML_SAVE_DIR.getPath());
      if (!Files.exists(outputDir))
        Files.createDirectories(outputDir);

      // 2. Generate the full file path
      String sanitizedFileName = fileName.replace(".md", ".html");
      Path outputPath = outputDir.resolve(sanitizedFileName);

      // 3. Check if the file already exists
      if (Files.exists(outputPath)) {
        log.info("File already exists. Skipping save: {}", outputPath);
        return;
      }

      // 4. Write the HTML content to the file
      Files.writeString(outputPath, htmlContent);

      log.info("HTML saved to {}", outputPath);
    } catch (IOException e) {
      log.error("Failed to save HTML file locally", e);
      throw new IllegalStateException("Failed to save HTML file locally", e);
    }
  }

}
