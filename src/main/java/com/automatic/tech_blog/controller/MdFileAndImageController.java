package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.request.UploadImageInfoRequest;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.service.MdFileAndImageService;
import jakarta.validation.Valid;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = {"/blog/api/v1/db"})
public class MdFileAndImageController {
  private final MdFileAndImageService mdFileAndImageService;

  /*TODO : Upload file infos into DB*/
  @PostMapping("/upload-files")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse uploadFilesInfo(@RequestBody FileLists requestBody) {
    log.info("Raw JSON Request Body: {}", requestBody);
    Object uploadResult = mdFileAndImageService.uploadFilesInfo(requestBody);

    if (uploadResult == null) throw new RuntimeException("Failed to upload files");

    return new ApiResponse(HttpStatus.CREATED.value(), uploadResult, new Date(), true);
  }

  /*TODO : Return file lists that is newly made or modified*/
  @GetMapping("/new-files")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse getNewFilesInfo() {
    return new ApiResponse(
        HttpStatus.OK.value(), mdFileAndImageService.getNewFilesInfo(), new Date(), true);
  }

  /*TODO : Upload pasted images into DB*/
  @PostMapping("/upload-images")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse uploadImagesInfo(
      @RequestBody @Valid GoogleAuthInfo authInfo, @RequestBody @Valid FileLists fileLists) {
    return new ApiResponse(
        HttpStatus.CREATED.value(),
        mdFileAndImageService.uploadImagesInfo(authInfo, fileLists),
        new Date(),
        true);
  }

  /*TODO : Return image lists that is pasted on the notes*/
  @GetMapping("/new-images")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse getNewImagesInfo() {
    return new ApiResponse(
        HttpStatus.OK.value(), mdFileAndImageService.getNewImagesInfo(), new Date(), true);
  }

  /*TODO : Update image info with image ID and URL*/
  @PutMapping("/update-image-info")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse updateImageInfo(@RequestBody @Valid UploadImageInfoRequest imageInfo) {
    mdFileAndImageService.updateImageInfo(imageInfo.imageId(), imageInfo.imageUrl());
    return new ApiResponse(HttpStatus.OK.value(), "Image info updated", new Date(), true);
  }
}
