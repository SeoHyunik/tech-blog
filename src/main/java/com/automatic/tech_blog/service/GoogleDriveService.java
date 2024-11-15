package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import java.util.List;

public interface GoogleDriveService {
  MdFileLists scanFiles(GoogleAuthInfo authInfo);

  List<ProcessedDataList> uploadFiles(MdFileLists mdFileLists);

  MdFileLists getNewFiles();
}
