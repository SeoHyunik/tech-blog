package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import reactor.core.publisher.Flux;

public interface WordPressService {
  Flux<ProcessedDataList> postArticlesToBlog(MdFileLists mdFileLists);
}
