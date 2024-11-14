package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.service.GoogleDriveService;
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
    public ApiResponse scanFiles(@RequestBody @Valid GoogleAuthInfo authInfo) {
        return new ApiResponse(
            HttpStatus.OK.value(),
            googleDriveService.scanFiles(authInfo),
            new Date(),
            true);
    }

    /*TODO : Upload file infos into DB*/
    @PostMapping("/upload-files")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse uploadFiles(@RequestBody @Valid MdFileLists mdFileLists) {
        return new ApiResponse(
            HttpStatus.CREATED.value(),
            googleDriveService.uploadFiles(mdFileLists),
            new Date(),
            true);
    }
}
