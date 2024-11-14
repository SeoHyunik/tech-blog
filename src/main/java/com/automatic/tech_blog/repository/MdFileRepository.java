package com.automatic.tech_blog.repository;

import com.automatic.tech_blog.entity.TbMdFiles;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MdFileRepository extends JpaRepository<TbMdFiles, Long> {
  Optional<TbMdFiles> findByFileId(String id);
}

