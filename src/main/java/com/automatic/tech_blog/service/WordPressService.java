package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import reactor.core.publisher.Flux;

public interface WordPressService {
  Flux<ProcessedDataList> postArticlesToBlog(FileLists fileLists);
  Flux<ProcessedDataList> uploadImages(ImageLists imageLists);
  void updateImageInfo(String imageId, String imageUrl);
}
