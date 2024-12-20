package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.FileInfo;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.enums.TargetFolders;
import com.automatic.tech_blog.utils.GoogleDriveUtils;
import com.google.api.services.drive.Drive;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveServiceImpl implements GoogleDriveService {

  private final GoogleDriveUtils driveUtils;

  @Override
  public FileLists scanFiles(GoogleAuthInfo authInfo) {
    try {
      // 1. Create the Drive service
      Drive driveService = driveUtils.createDriveService(authInfo, "kiwijam");

      // 2. Scan for .md files in specific directories
      List<String> targetFolders = TargetFolders.getFolderNames();
      List<FileInfo> fileInfos = new ArrayList<>();

      for (String folderName : targetFolders) {
        // 3. Find the target directory by name
        String folderId = driveUtils.findFolderIdByName(driveService, folderName);
        if (folderId != null) {
          // 4. If folder is found, scan for .md files within it
          driveUtils.findMdFilesInDirectory(driveService, folderId, fileInfos, folderName);
        } else {
          log.warn("Folder {} not found.", folderName);
        }
      }

      return new FileLists(fileInfos);
    } catch (Exception e) {
      log.error("Error occurred while scanning files from Google Drive: {}", e.getMessage(), e);
      throw new IllegalStateException("Error occurred while scanning files from Google Drive", e);
    }
  }
}
