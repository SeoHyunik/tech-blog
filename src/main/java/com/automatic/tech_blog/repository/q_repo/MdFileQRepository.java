package com.automatic.tech_blog.repository.q_repo;

import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.entity.TbMdFiles;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface MdFileQRepository {
  Optional<TbMdFiles> findByFileId(String fileId);
  List<MdFileInfo> findNewFiles(Date since);
}
