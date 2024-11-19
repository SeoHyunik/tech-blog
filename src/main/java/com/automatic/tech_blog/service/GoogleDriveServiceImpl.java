package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.entity.TbMdFiles;
import com.automatic.tech_blog.enums.TargetFolders;
import com.automatic.tech_blog.repository.MdFileRepository;
import com.automatic.tech_blog.utils.GoogleDriveUtils;
import com.google.api.services.drive.Drive;
import java.util.ArrayList;
import java.util.Calendar;
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

  private final GoogleDriveUtils driveUtils;
  private final MdFileRepository mdFileRepository;

  @Override
  public MdFileLists scanFiles(GoogleAuthInfo authInfo) {
    try {
      // Step 1: Create the Drive service
      Drive driveService = driveUtils.createDriveService(authInfo, "kiwijam");

      // Step 2: Scan for .md files in specific directories
      List<String> targetFolders = TargetFolders.getFolderNames();
      List<MdFileInfo> mdFileInfos = new ArrayList<>();

      for (String folderName : targetFolders) {
        // Find the target directory by name
        String folderId = driveUtils.findFolderIdByName(driveService, folderName);
        if (folderId != null) {
          // If folder is found, scan for .md files within it
          driveUtils.findMdFilesInDirectory(driveService, folderId, mdFileInfos, folderName);
        } else {
          log.warn("Folder {} not found.", folderName);
        }
      }

      return new MdFileLists(mdFileInfos);
    } catch (Exception e) {
      log.error("Error occurred while scanning files from Google Drive: {}", e.getMessage(), e);
      throw new IllegalStateException("Error occurred while scanning files from Google Drive", e);
    }
  }


  @Override
  @Transactional
  public List<ProcessedDataList> uploadFiles(MdFileLists mdFileLists) {
    // 1. Create a list to store processed data
    List<ProcessedDataList> processedData = new ArrayList<>();

    mdFileLists.mdFileLists().forEach(mdFileInfo -> {
      try {
        // 2. Fetch existing record by fileId if it exists
        Optional<TbMdFiles> existingFile = mdFileRepository.findByFileId(mdFileInfo.id());

        String newFilePath = mdFileInfo.directory();

        if (existingFile.isPresent()) {
          TbMdFiles fileInfo = existingFile.get();

          // 3. Check if either modifiedAt or filePath has changed
          boolean isModifiedAtDifferent = !Objects.equals(mdFileInfo.modifiedAt(), fileInfo.getModifiedAt());
          boolean isFilePathDifferent = !Objects.equals(newFilePath, fileInfo.getFilePath());

          if (isModifiedAtDifferent || isFilePathDifferent) {
            fileInfo.setModifiedAt(mdFileInfo.modifiedAt());  // Update modifiedAt only if different
            fileInfo.setFileName(mdFileInfo.fileName());  // Always update fileName
            fileInfo.setFilePath(newFilePath);  // Update filePath only if different
            mdFileRepository.save(fileInfo);
            log.info("Updated file with ID: {} and Name: {}", fileInfo.getFileId(), fileInfo.getFileName());
          }

          // 4. Add to processed data list
          processedData.add(new ProcessedDataList(fileInfo.getFileId(), fileInfo.getFileName()));
        } else {
          // 5. Insert new file
          TbMdFiles entity = new TbMdFiles();
          entity.setFileId(mdFileInfo.id());
          entity.setFileName(mdFileInfo.fileName());
          entity.setFilePath(newFilePath);
          entity.setCreatedAt(mdFileInfo.createdAt());
          entity.setModifiedAt(mdFileInfo.modifiedAt());
          mdFileRepository.save(entity);

          log.info("Inserted new file with ID: {} and Name: {}", entity.getFileId(), entity.getFileName());

          // 6. Add to processed data list
          processedData.add(new ProcessedDataList(entity.getFileId(), entity.getFileName()));
        }
      } catch (DataIntegrityViolationException | ConstraintViolationException e) {
        log.error("Duplicate fileId encountered for file: {}", mdFileInfo.fileName());
        throw new IllegalStateException("Error occurred while uploading files", e);
      }
    });

    // 7. Return processed data list
    return processedData;
  }


  @Override
  public MdFileLists getNewFiles() {
    // 1. Calculate the timestamp for 24 hours ago
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR, -24);
    Date since = calendar.getTime();

    // 2. Find new files created or modified since the calculated timestamp
    List<TbMdFiles> newFiles = mdFileRepository.findNewFiles(since);

    // 3. Convert TbMdFiles to MdFileInfo
    List<MdFileInfo> mdFileInfos = newFiles.stream()
        .map(file -> new MdFileInfo(
            file.getFileName(),
            file.getFileId(),
            file.getFilePath(),
            file.getCreatedAt(),
            file.getModifiedAt(),
            file.getDeletedAt()))
        .toList();

    return new MdFileLists(mdFileInfos);
  }
}
