package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.entity.TbMdFiles;
import com.automatic.tech_blog.enums.ExternalUrls;
import com.automatic.tech_blog.enums.TargetFolders;
import com.automatic.tech_blog.repository.MdFileRepository;
import com.automatic.tech_blog.utils.FunctionUtils;
import com.automatic.tech_blog.utils.GoogleAuthUtils;
import com.automatic.tech_blog.utils.GoogleDriveUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveServiceImpl implements GoogleDriveService {

  private final GoogleAuthUtils authUtils;
  private final GoogleDriveUtils driveUtils;
  private final MdFileRepository mdFileRepository;

  @Override
  public MdFileLists scanFiles(GoogleAuthInfo authInfo) {
    try {
      HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
      JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

      // 1. Load Google Credentials and obtain fresh access token
      GoogleCredentials credentials = authUtils.getGoogleCredentials(authInfo);
      if (credentials.createScopedRequired()) {
        credentials = credentials.createScoped(Collections.singletonList(ExternalUrls.GOOGLE_DRIVE_METADATA_READONLY.getUrl()));
      }
      credentials.refreshIfExpired();

      HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

      // 2. Build the Drive service
      Drive driveService = new Drive.Builder(transport, jsonFactory, requestInitializer)
          .setApplicationName("Tech Blog")
          .build();

      // 3. Find specific directories and scan for .md files in them
      List<String> targetFolders = TargetFolders.getFolderNames();
      List<MdFileInfo> mdFileInfos = new ArrayList<>();

      for (String folderName : targetFolders) {
        // 4. Find the target directory by name
        String folderId = driveUtils.findFolderIdByName(driveService, folderName);
        if (folderId != null)  // 5. If folder is found, scan for .md files within it
          driveUtils.findMdFilesInDirectory(driveService, folderId, mdFileInfos, folderName);
      }

      return new MdFileLists(mdFileInfos);
    } catch (Exception e) {
      log.error("Error occurred while scanning files from Google Drive: {}", e.getMessage(), e);
      throw new IllegalStateException("Error occurred while scanning files from Google Drive", e);
    }
  }

  @Override
  @Transactional
  public String uploadFiles(MdFileLists mdFileLists) {
    mdFileLists.mdFileLists().forEach(mdFileInfo -> {
      try {
        // Fetch existing record by fileId if it exists
        Optional<TbMdFiles> existingFile = mdFileRepository.findByFileId(mdFileInfo.id());

        Date modifiedAt = FunctionUtils.convertGoogleDateTimeToDate(mdFileInfo.modifiedAt());
        String newFilePath = mdFileInfo.directory();

        if (existingFile.isPresent()) {
          TbMdFiles fileInfo = existingFile.get();

          // Check if either modifiedAt or filePath has changed
          boolean isModifiedAtDifferent = !Objects.equals(modifiedAt, fileInfo.getModifiedAt());
          boolean isFilePathDifferent = !Objects.equals(newFilePath, fileInfo.getFilePath());

          if (isModifiedAtDifferent || isFilePathDifferent) {
            fileInfo.setModifiedAt(modifiedAt);  // Update modifiedAt only if different
            fileInfo.setFileName(mdFileInfo.fileName());  // Always update fileName
            fileInfo.setFilePath(newFilePath);  // Update filePath only if different
            mdFileRepository.save(fileInfo);
            log.info("Updated file with ID: {} and Name: {}", fileInfo.getFileId(), fileInfo.getFileName());
          }
        } else {
          // Insert new file
          TbMdFiles entity = new TbMdFiles();
          entity.setFileId(mdFileInfo.id());
          entity.setFileName(mdFileInfo.fileName());
          entity.setFilePath(newFilePath);
          entity.setCreatedAt(FunctionUtils.convertGoogleDateTimeToDate(mdFileInfo.createdAt()));
          entity.setModifiedAt(modifiedAt);
          mdFileRepository.save(entity);
        }
      } catch (DataIntegrityViolationException | ConstraintViolationException e) {
        log.error("Duplicate fileId encountered for file: {}", mdFileInfo.fileName());
        throw new IllegalStateException("Error occurred while uploading files", e);
      }
    });
    return "Files uploaded successfully";
  }

}
