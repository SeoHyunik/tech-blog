package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.FileLists;

public interface GoogleDriveService {
  FileLists scanFiles(GoogleAuthInfo authInfo);
}
