package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.request.EditTechNotesRequest;
import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.service.OpenAiService;
import jakarta.validation.Valid;
import java.util.Date;
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
@RequestMapping(value = {"/blog/api/v1/open-ai"})
public class OpenAiController {
  private final OpenAiService openAiService;

  /* TODO: Transform the contents of Markdown files into a blog-friendly format
      and convert them to HTML (markup) for WordPress upload */
  @PostMapping("/edit-tech-notes")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse editTechNotes(@RequestBody @Valid EditTechNotesRequest request) {
    // 1. Extract GoogleAuthInfo and MdFileLists
    GoogleAuthInfo googleAuthInfo = request.googleAuthInfo();
    MdFileLists mdFileLists = request.mdFileLists();

    // 2. Call the service with extracted data
    return new ApiResponse(
        HttpStatus.CREATED.value(),
        openAiService.editTechNotes(mdFileLists, googleAuthInfo),
        new Date(),
        true
    );
  }

}
