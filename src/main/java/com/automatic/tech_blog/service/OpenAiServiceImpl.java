package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.EditTechNotesRequest;
import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.request.OpenAiRequest;
import com.automatic.tech_blog.dto.response.OpenAiResponse;
import com.automatic.tech_blog.dto.service.FileInfo;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.enums.InternalPaths;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.automatic.tech_blog.repository.q_repo.PastedImageQRepository;
import com.automatic.tech_blog.utils.FileUtils;
import com.automatic.tech_blog.utils.GoogleDriveUtils;
import com.automatic.tech_blog.utils.OpenAiUtils;
import com.automatic.tech_blog.utils.SecurityUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {
  private final GoogleDriveUtils googleDriveUtils;
  private final OpenAiUtils openAiUtils;
  private final PastedImageQRepository imageQRepository;

  @Override
  public Flux<ProcessedDataList> editTechNotes(EditTechNotesRequest request) {
    // 1. Scan the local directory for existing HTML files (Sync Job)
    Set<String> existingHtmlFiles = FileUtils.getExistingHtmlFiles();

    // 2. Filter and process markdown files (Async Job)
    return Flux.fromIterable( request.fileLists().fileLists())
        .filter(mdFileInfo -> !existingHtmlFiles.contains(mdFileInfo.fileName().replace(".md", ".html")))
        .flatMap(mdFileInfo -> processMarkdownFileAsync(mdFileInfo, request.googleAuthInfo()));
  }



  private Mono<ProcessedDataList> processMarkdownFileAsync(FileInfo fileInfo, GoogleAuthInfo googleAuthInfo) {
    return Mono.fromCallable(() -> googleDriveUtils.createDriveService(googleAuthInfo, "kiwijam"))
        .flatMap(driveService -> Mono.fromCallable(() ->
            googleDriveUtils.getFileContent(driveService, fileInfo.id(), googleAuthInfo)))
        .flatMap(fileContent -> fileContent != null
            ? transformMarkdownToHtml(fileContent, fileInfo.id(), fileInfo.fileName())
            : Mono.fromRunnable(() -> log.info("File content is null for file ID: {}", fileInfo.id()))
        );
  }

  private Mono<ProcessedDataList> transformMarkdownToHtml(String markdownContent, String fileId, String fileName) {
    return Mono.fromCallable(() -> {
      // 1. Load the editor roles JSON
      JsonObject roles = loadEditorRoles();

      // 2. Create the prompt JSON
      String prompt = createPrompt(roles, markdownContent);

      // 3. Get the OpenAI API key
      String apiKey = SecurityUtils.decryptAuthFile(SecuritySpecs.OPEN_AI_SECRET_KEY_FILE_PATH.getValue());

      // 4. Generate HTML content from the markdown
      OpenAiRequest openAiRequest = new OpenAiRequest(prompt, apiKey);
      OpenAiResponse openAiResponse = openAiUtils.generateHtmlFromMarkdown(openAiRequest);

      // 5. Edit image tags in the HTML content
      String content = editImageTags(openAiResponse.content());

      // 6. Save the HTML content to a local file
      saveHtmlToLocal(content, fileName);

      return new ProcessedDataList(fileId, fileName);
    });
  }

  private String editImageTags(String content) {
    // 1. Use regex to find Markdown image tags: ![[image_name]]
    Pattern pattern = Pattern.compile("!\\[\\[(.*?)]]");
    Matcher matcher = pattern.matcher(content);

    // 2. Create a StringBuilder for the resulting string
    StringBuilder updatedContent = new StringBuilder();

    while (matcher.find()) {
      // 3. Extract the image name from the tag
      String imageName = matcher.group(1);

      // 4. Fetch the image URL from the database
      String imageUrl = imageQRepository.findByImageName(imageName);

      if (imageUrl != null) {
        // 5.1 Replace the Markdown tag with the new one containing the image URL
        String newMarkdownTag = "![[" + imageUrl + "]]";
        matcher.appendReplacement(updatedContent, newMarkdownTag);
      } else {
        // 5.2 If the image URL is not found, keep the original tag
        matcher.appendReplacement(updatedContent, matcher.group(0));
      }
    }
    matcher.appendTail(updatedContent);

    return updatedContent.toString();
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
