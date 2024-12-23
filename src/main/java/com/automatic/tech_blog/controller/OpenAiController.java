package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.request.EditTechNotesRequest;
import com.automatic.tech_blog.dto.response.ApiResponse;
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
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = {"/blog/api/v1/open-ai"})
public class OpenAiController {
  private final OpenAiService openAiService;

  /*TODO : Edit tech-notes by Open AI API with proper prompt and rules*/
  @PostMapping("/edit-tech-notes")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<ApiResponse> editTechNotes(@RequestBody @Valid EditTechNotesRequest request) {
    // 1. Call the service and wrap the response in a Mono
    return openAiService
        .editTechNotes(request)
        .collectList()
        .map(result -> new ApiResponse(HttpStatus.CREATED.value(), result, new Date(), true));
  }
}
