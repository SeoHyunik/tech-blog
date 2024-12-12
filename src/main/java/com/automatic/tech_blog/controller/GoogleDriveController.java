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
    public ApiResponse uploadFiles(@RequestBody String rawRequestBody) {
        log.info("Raw JSON Request Body: {}", rawRequestBody);

        // ObjectMapper 초기화
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Java 8+ 시간 API 지원
            .registerModule(new ParameterNamesModule()) // Record 지원
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 날짜를 ISO-8601로 출력

        FileLists fileLists;
        try {
            // JSON 데이터를 FileLists 타입으로 변환
            fileLists = objectMapper.readValue(rawRequestBody, FileLists.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to FileLists: {}", rawRequestBody, e);
            throw new RuntimeException("Invalid JSON format", e);
        }

        log.info("Converted FileLists: {}", fileLists);

        // googleDriveService 호출
        Object uploadResult = googleDriveService.uploadFiles(fileLists);

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
