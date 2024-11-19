package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = {"/blog/api/v1/wp"})
public class WordPressController {
  /*TODO: Upload html file on WordPress Blog*/
  @PostMapping("/upload-html")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse uploadHtml() {
    return null;
  }
}
