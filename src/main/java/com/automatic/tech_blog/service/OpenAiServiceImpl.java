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
import com.google.api.client.json.Json;
import com.google.api.services.drive.Drive;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

      // 2. Process each Markdown file in the list
      List<ProcessedDataList> processedData = new ArrayList<>();
      for (MdFileInfo mdFileInfo : mdFileLists.mdFileLists()) {
        String fileContent = googleDriveUtils.getFileContent(driveService, mdFileInfo.id(), googleAuthInfo);

        if (fileContent != null) {
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
      // 1. Load editor rules
      JsonObject roles = loadEditorRoles();
      // Create the prompt with the updated messages
      String prompt = createPrompt(roles, markdownContent);

      // 2. Get Open AI Api_key
      String apiKey = SecurityUtils.decryptOpenAiApiKey(SecuritySpecs.OPEN_AI_SECRET_KEY_FILE_PATH.getValue());

      // 2. Use OpenAI API to convert Markdown to HTML
      OpenAiRequest openAiRequest = new OpenAiRequest(prompt, apiKey);
      OpenAiResponse openAiResponse = openAiUtils.generateHtmlFromMarkdown(openAiRequest);

      // 3. Save the HTML to a local directory
      saveHtmlToLocal(openAiResponse.content(), fileName);

      log.info("Token Usage: {}", openAiResponse.tokenUsage());

      // 4. Return the processed data
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
      if (messages == null) {
        throw new IllegalStateException("Missing 'messages' array in roles JSON");
      }

      // 2. Create a new message for the markdown content
      JsonObject newMessage = new JsonObject();
      newMessage.addProperty("role", "user");
      newMessage.addProperty("content", "Markdown Content:\n" + markdownContent);

      // 3. Add the new message to the messages array
      messages.add(newMessage);

      // 4. Update the roles object with the modified messages array (optional if roles is mutable)
      roles.add("messages", messages);

      // 5. Return the updated roles object as a JSON string (or JsonObject if preferred)
      return roles.toString(); // If you need JSON string
      // return roles; // If you need JsonObject directly
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create prompt JSON", e);
    }
  }

  private JsonObject loadEditorRoles() {
    try {
      // 1. Load the JSON file from the given path
      Path path = Paths.get(InternalPaths.EDITOR_ROLES.getPath());
      String jsonString = Files.readString(path);

      return JsonParser.parseString(jsonString).getAsJsonObject();
    } catch (IOException e) {
      log.error("Failed to load editor rules from {}", InternalPaths.EDITOR_ROLES.getPath(), e);
      throw new IllegalStateException("Failed to load editor rules", e);
    } catch (Exception e) {
      log.error("Failed to parse editor rules JSON from {}", InternalPaths.EDITOR_ROLES.getPath(), e);
      throw new IllegalStateException("Invalid JSON format in editor rules", e);
    }
  }

  private void saveHtmlToLocal(String htmlContent, String fileName) {
    try {
      // Ensure the directory exists
      Path outputDir = Paths.get(InternalPaths.HTML_SAVE_DIR.getPath());
      if (!Files.exists(outputDir)) {
        Files.createDirectories(outputDir);
      }

      // Generate the full file path
      String sanitizedFileName = fileName.replace(".md", ".html");
      Path outputPath = outputDir.resolve(sanitizedFileName);

      // Write the HTML content to the file
      Files.writeString(outputPath, htmlContent);

      log.info("HTML saved to {}", outputPath);
    } catch (IOException e) {
      log.error("Failed to save HTML file locally", e);
      throw new IllegalStateException("Failed to save HTML file locally", e);
    }
  }
}
