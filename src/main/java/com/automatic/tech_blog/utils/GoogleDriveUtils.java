package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleDriveUtils {

  /**
   * Finds the folder ID by name in Google Drive.
   */
  public String findFolderIdByName(Drive driveService, String folderName) throws IOException {
    FileList result = driveService.files().list()
        .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "'")
        .setFields("files(id, name)")
        .execute();

    List<File> files = result.getFiles();
    if (files != null && !files.isEmpty()) {
      return files.get(0).getId();  // Return the ID of the first matched folder
    }
    return null;
  }

  /**
   * Scans for .md files within a given directory ID and adds them to the result list.
   */
  public void findMdFilesInDirectory(Drive driveService, String directoryId,
      List<MdFileInfo> mdFileInfos, String parentFolderName) throws IOException {

    FileList result = driveService.files().list()
        .setQ("'" + directoryId + "' in parents and mimeType != 'application/vnd.google-apps.folder'")
        .setFields("files(id, name, createdTime, modifiedTime)")  // Include createdTime and modifiedTime
        .execute();

    List<File> files = result.getFiles();
    if (files != null && !files.isEmpty()) {
      for (File file : files) {
        if (file.getName().endsWith(".md")) {
          String fileName = new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
          DateTime createdTime = file.getCreatedTime();
          DateTime modifiedTime = file.getModifiedTime();

          // Add file details including folder name, createdTime, and modifiedTime
          mdFileInfos.add(new MdFileInfo(fileName, file.getId(), parentFolderName, createdTime, modifiedTime, null));
        }
      }
    }
  }

}
