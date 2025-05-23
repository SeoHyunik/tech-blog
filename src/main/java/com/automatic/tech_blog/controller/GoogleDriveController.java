package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.service.GoogleDriveService;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
        HttpStatus.OK.value(), googleDriveService.scanFiles(authInfo), new Date(), true);
  }
}
