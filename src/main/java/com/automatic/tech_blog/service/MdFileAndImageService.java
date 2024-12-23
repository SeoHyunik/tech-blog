package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import java.util.List;

public interface MdFileAndImageService {
  List<ProcessedDataList> uploadFilesInfo(FileLists fileLists);

  FileLists getNewFilesInfo();

  List<ProcessedDataList> uploadImagesInfo(GoogleAuthInfo authInfo, FileLists fileLists);

  ImageLists getNewImagesInfo();

  void updateImageInfo(String imageId, String imageUrl);
}
