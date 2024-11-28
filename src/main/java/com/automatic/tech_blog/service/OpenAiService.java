package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.EditTechNotesRequest;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import reactor.core.publisher.Flux;

public interface OpenAiService {
  Flux<ProcessedDataList> editTechNotes(EditTechNotesRequest request);
}
