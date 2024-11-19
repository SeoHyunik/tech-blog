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
      String rules = loadEditorRules();
      String prompt = rules + "\nMarkdown Content:\n" + markdownContent;

      // 2. Get Open AI Api_key
      String apiKey = SecurityUtils.decryptOpenAiApiKey(SecuritySpecs.OPEN_AI_API_KEY_FILE_PATH.getValue());

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

  private String loadEditorRules() {
    try {
      Path path = Paths.get(InternalPaths.EDITOR_ROLES.getPath());
      return Files.readString(path);
    } catch (IOException e) {
      log.error("Failed to load editor rules from {}", InternalPaths.EDITOR_ROLES.getPath(), e);
      throw new IllegalStateException("Failed to load editor rules", e);
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
