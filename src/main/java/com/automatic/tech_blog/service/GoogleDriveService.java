package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import java.util.List;

public interface GoogleDriveService {
  FileLists scanFiles(GoogleAuthInfo authInfo);
  List<ProcessedDataList> uploadFiles(FileLists fileLists);
  FileLists getNewFiles();
  List<ProcessedDataList> uploadPastedImages(FileLists fileLists);

}
