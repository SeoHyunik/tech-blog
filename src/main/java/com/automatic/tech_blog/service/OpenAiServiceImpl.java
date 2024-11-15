package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.utils.GoogleDriveUtils;
import com.google.api.services.drive.Drive;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService{
  private final GoogleDriveUtils googleDriveUtils;
  @Override
  public List<ProcessedDataList> editTechNotes(MdFileLists mdFileLists, GoogleAuthInfo googleAuthInfo) {
    try {
      // Step 1: Create the Drive service
      Drive driveService = googleDriveUtils.createDriveService(googleAuthInfo, "Tech Blog");

      // Step 2: Process each Markdown file in the list
      List<ProcessedDataList> processedData = new ArrayList<>();
      for (MdFileInfo mdFileInfo : mdFileLists.mdFileLists()) {
        // Fetch the content of the file from Google Drive
        String fileContent = googleDriveUtils.getFileContent(driveService, mdFileInfo.id());

        if (fileContent != null) {
          // Use OpenAI to transform the content into HTML
          String htmlContent = transformMarkdownToHtml(fileContent);


          // Add the processed file to the response list
          processedData.add(new ProcessedDataList(mdFileInfo.id(), mdFileInfo.fileName()));
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

  private String transformMarkdownToHtml(String markdownContent, Drive driveService, String destinationFolderId) {
    try {
      // Step 1: Load editor rules from editor_rules.yml
      String rules = loadEditorRules();
      String prompt = rules + "\nMarkdown Content:\n" + markdownContent;

      // Step 2: Use OpenAI API to convert Markdown to HTML
      String htmlContent = openAiClient.generateHtmlFromMarkdown(prompt); // Assume API client is configured

      // Step 3: Save the HTML content to Google Drive
      String fileId = saveHtmlToDrive(htmlContent, driveService, destinationFolderId);
      log.info("HTML content successfully saved to Google Drive with file ID: {}", fileId);

      return fileId;
    } catch (Exception e) {
      log.error("Error converting Markdown to HTML or saving it: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to transform and save HTML content", e);
    }
  }


}
