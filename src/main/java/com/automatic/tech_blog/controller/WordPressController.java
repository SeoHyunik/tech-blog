package com.automatic.tech_blog.controller;

import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.service.WordPressService;
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
@RequestMapping(value = {"/blog/api/v1/wp"})
public class WordPressController {
  private final WordPressService wpService;

  /*TODO : Post ai-edited articles to WordPress blog*/
  @PostMapping("/post-articles")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<ApiResponse> postArticles(@RequestBody @Valid FileLists fileLists) {
    return wpService.postArticlesToBlog(fileLists)
        .collectList()
        .map(processedDataList -> new ApiResponse(
            HttpStatus.CREATED.value(),
            processedDataList,
            new Date(),
            true
        ));
  }

  /*TODO : Upload images to WordPress media library*/
  @PostMapping("/upload-images")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<ApiResponse> uploadImages(@RequestBody @Valid ImageLists imageLists) {
    return wpService.uploadImages(imageLists)
        .collectList()
        .map(processedDataList -> new ApiResponse(
            HttpStatus.CREATED.value(),
            processedDataList,
            new Date(),
            true
        ));
  }
}
