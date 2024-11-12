package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;

public interface GoogleService {
  MdFileLists scanFiles(GoogleAuthInfo authInfo);
}
