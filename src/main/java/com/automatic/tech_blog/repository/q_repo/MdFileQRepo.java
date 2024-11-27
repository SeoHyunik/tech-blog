package com.automatic.tech_blog.repository.q_repo;

import com.automatic.tech_blog.dto.service.MdFileInfo;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface MdFileQRepo {
  List<MdFileInfo> findNewFiles(Date since);
}
