package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.FileInfo;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.entity.TbAttachedImages;
import com.automatic.tech_blog.entity.TbMdFiles;
import com.automatic.tech_blog.repository.MdFileRepository;
import com.automatic.tech_blog.repository.PastedImageRepository;
import com.automatic.tech_blog.repository.q_repo.MdFileQRepository;
import com.automatic.tech_blog.repository.q_repo.PastedImageQRepository;
import com.automatic.tech_blog.utils.GoogleDriveUtils;
import com.google.api.services.drive.Drive;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MdFileAndImageServiceImpl implements MdFileAndImageService{
  private final GoogleDriveUtils driveUtils;
  private final MdFileRepository mdFileRepository;
  private final MdFileQRepository mdFileQRepository;
  private final PastedImageRepository pastedImageRepository;
  private final PastedImageQRepository pastedImageQRepository;

  @Override
  @Transactional
  public List<ProcessedDataList> uploadFilesInfo(FileLists fileLists) {
    // 1. Create a list to store processed data
    List<ProcessedDataList> processedData = new ArrayList<>();

    fileLists.fileLists().stream()
        .filter(fileInfo -> driveUtils.isMdFile(fileInfo.fileName())) // Filter out non-Markdown files
        .forEach(fileInfo -> {
          try {
            // 2. Fetch existing record by fileId if it exists
            Optional<TbMdFiles> existingFile = mdFileQRepository.findByFileId(fileInfo.id());

            String newFilePath = fileInfo.directory();

            if (existingFile.isPresent()) {
              TbMdFiles tbMdFileInfo = existingFile.get();

              // 3. Check if either modifiedAt or filePath has changed
              boolean isModifiedAtDifferent = !Objects.equals(fileInfo.modifiedAt(), tbMdFileInfo.getModifiedAt());
              boolean isFilePathDifferent = !Objects.equals(newFilePath, tbMdFileInfo.getFilePath());

              if (isModifiedAtDifferent || isFilePathDifferent) {
                tbMdFileInfo.setModifiedAt(fileInfo.modifiedAt());  // Update modifiedAt only if different
                tbMdFileInfo.setFileName(fileInfo.fileName());  // Always update fileName
                tbMdFileInfo.setFilePath(newFilePath);  // Update filePath only if different
                mdFileRepository.save(tbMdFileInfo);
                log.info("Updated file with ID: {} and Name: {}", tbMdFileInfo.getFileId(), tbMdFileInfo.getFileName());
              }

              // 4. Add to processed data list
              processedData.add(new ProcessedDataList(tbMdFileInfo.getFileId(), tbMdFileInfo.getFileName()));
            } else {
              // 5. Insert new file
              TbMdFiles entity = new TbMdFiles();
              entity.setFileId(fileInfo.id());
              entity.setFileName(fileInfo.fileName());
              entity.setFilePath(newFilePath);
              entity.setCreatedAt(fileInfo.createdAt());
              entity.setModifiedAt(fileInfo.modifiedAt());
              mdFileRepository.save(entity);

              log.info("Inserted new file with ID: {} and Name: {}", entity.getFileId(), entity.getFileName());

              // 6. Add to processed data list
              processedData.add(new ProcessedDataList(entity.getFileId(), entity.getFileName()));
            }
          } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            log.error("Duplicate fileId encountered for file: {}", fileInfo.fileName());
            throw new IllegalStateException("Error occurred while uploading files", e);
          }
        });
    return processedData;
  }

  @Override
  public FileLists getNewFilesInfo() {
    // 1. Fetch new files from the database using Query DSL
    return new FileLists(mdFileQRepository.findNewFiles(get24HoursAgo()));
  }

  @Override
  @Transactional
  public List<ProcessedDataList> uploadImagesInfo(GoogleAuthInfo authInfo, FileLists fileLists) {
    try {
      // 1. Create the Drive service
      Drive driveService = driveUtils.createDriveService(authInfo, "kiwijam");

      // 2. Create a list to store processed data
      List<ProcessedDataList> processedData = new ArrayList<>();

      // 3. Process each file
      fileLists.fileLists().stream()
          .filter(fileInfo -> !driveUtils.isMdFile(fileInfo.fileName())) // Filter out Markdown files
          .forEach(fileInfo -> processFile(driveService, fileInfo, processedData));

      return processedData;
    } catch (Exception e) {
      log.error("Error occurred while processing files: {}", e.getMessage(), e);
      throw new IllegalStateException("An error occurred while uploading pasted images", e);
    }
  }

  @Override
  public ImageLists getNewImagesInfo() {
    // 1. Fetch new images from the database using Query DSL
    return pastedImageQRepository.findNewImages(get24HoursAgo()).orElse(new ImageLists(new ArrayList<>()));
  }

  @Override
  public void updateImageInfo(String imageId, String imageUrl) {
    Optional<TbAttachedImages> image = pastedImageQRepository.findByImageId(imageId);
    if (image.isPresent()) {
      TbAttachedImages updatedImage = image.get();
      updatedImage.setImageUrl(imageUrl);
      updatedImage.setUploadedAt(new Date());
      pastedImageRepository.save(updatedImage);
    } else {
      log.warn("Image not found in the database: {}", imageId);
    }
  }

  private void processFile(Drive driveService, FileInfo fileInfo, List<ProcessedDataList> processedData) {
    try {
      // 1. Check if the image already exists
      Optional<TbAttachedImages> existingImage = pastedImageQRepository.findByImageId(fileInfo.id());

      if (existingImage.isEmpty()) {
        // 2. Create a new image record
        TbAttachedImages entity = new TbAttachedImages();
        entity.setImageId(fileInfo.id());
        entity.setImageName(fileInfo.fileName());
        entity.setImageFilePath(driveUtils.findFilePathById(driveService, fileInfo.id()));
        entity.setCreatedAt(fileInfo.createdAt());
        pastedImageRepository.save(entity);
        log.info("Inserted new image with ID: {}, Name: {}, Path: {}", entity.getImageId(), entity.getImageName(), entity.getImageFilePath());

        // 3. Add to processed data list
        processedData.add(new ProcessedDataList(entity.getImageId(), entity.getImageName()));
      }
    } catch (DataIntegrityViolationException | ConstraintViolationException e) {
      log.error("Duplicate imageId encountered for image: {}", fileInfo.fileName());
      throw new IllegalStateException("Error occurred while uploading images", e);
    } catch (IOException e) {
      log.error("Error occurred while fetching file path from Google Drive for image: {}", fileInfo.fileName(), e);
      throw new RuntimeException(e);
    }
  }

  private Date get24HoursAgo() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR, -24);
    return calendar.getTime();
  }
}
