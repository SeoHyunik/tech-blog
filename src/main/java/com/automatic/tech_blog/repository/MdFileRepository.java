package com.automatic.tech_blog.repository;

import com.automatic.tech_blog.entity.TbMdFiles;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MdFileRepository extends JpaRepository<TbMdFiles, Long> {
  Optional<TbMdFiles> findByFileId(String id);

  @Query("SELECT f FROM TbMdFiles f WHERE f.createdAt >= :since OR f.modifiedAt >= :since")
  List<TbMdFiles> findNewFiles(@Param("since") Date since);
}

