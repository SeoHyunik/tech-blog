package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.service.GoogleService;
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

    private final GoogleService googleService;

    /*TODO : Return file lists that is newly made*/
    @PostMapping("/scan-files")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse scanFiles(@RequestBody @Valid GoogleAuthInfo authInfo) {
        System.out.println("controller init : " + authInfo.toString());
        return new ApiResponse(
            HttpStatus.OK.value(),
            googleService.scanFiles(authInfo),
            new Date(),
            true);
    }
}
