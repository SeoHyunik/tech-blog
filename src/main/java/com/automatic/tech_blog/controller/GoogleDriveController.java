package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.service.GoogleDriveService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = {"/blog/api/v1/google"})
public class GoogleDriveController {

    private final GoogleDriveService googleDriveService;

    /*TODO : Return file lists that is newly made*/
    @GetMapping("/scan-files")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse scanFiles(@RequestHeader("Authorization") String authorizationHeader) {
        GoogleAuthInfo authInfo = new GoogleAuthInfo(authorizationHeader);
        return new ApiResponse(
            HttpStatus.OK.value(),
            googleDriveService.scanFiles(authInfo),
            new Date(),
            true
        );
    }

    /*TODO : Upload file infos into DB*/
    @PostMapping("/upload-files")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse uploadFiles(@RequestBody FileLists requestBody) {
        log.info("Raw JSON Request Body: {}", requestBody);
        Object uploadResult = googleDriveService.uploadFiles(requestBody);

        if (uploadResult == null) {
            throw new RuntimeException("Failed to upload files");
        }

        return new ApiResponse(
            HttpStatus.CREATED.value(),
            uploadResult,
            new Date(),
            true
        );
    }

    /*TODO : Return file lists that is newly made or modified*/
    @GetMapping("/new-files")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse getNewFiles() {
        return new ApiResponse(
            HttpStatus.OK.value(),
            googleDriveService.getNewFiles(),
            new Date(),
            true);
    }

    /*TODO : Return image lists that is pasted on the notes*/
    @GetMapping("/new-images")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse getNewImages() {
        return new ApiResponse(
            HttpStatus.OK.value(),
            googleDriveService.getNewImages(),
            new Date(),
            true);
    }

    /*TODO : Upload pasted images into DB*/
    @PostMapping("/upload-pasted-images")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse uploadPastedImages(@RequestBody @Valid GoogleAuthInfo authInfo, @RequestBody @Valid FileLists fileLists) {
        return new ApiResponse(
            HttpStatus.CREATED.value(),
            googleDriveService.uploadPastedImages(authInfo, fileLists),
            new Date(),
            true);
    }
}
