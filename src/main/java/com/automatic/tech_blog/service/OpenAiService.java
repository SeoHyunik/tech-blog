package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import java.util.List;

public interface OpenAiService {

  public List<ProcessedDataList> editTechNotes(MdFileLists mdFileLists, GoogleAuthInfo googleAuthInfo);
}
