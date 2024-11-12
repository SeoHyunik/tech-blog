package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.service.GoogleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = {"/blog/api/v1/google"})
public class GoogleDriverController {

    private final GoogleService googleService;

    /*TODO : Return file lists that is newly made*/
    @GetMapping("/scan-files")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse scanFiles(@RequestBody GoogleAuthInfo authInfo) {
        MdFileLists mdFileLists = googleService.scanFiles(authInfo);
        return new ApiResponse(HttpStatus.OK.value(), mdFileLists, new Date(), true);
    }
}
