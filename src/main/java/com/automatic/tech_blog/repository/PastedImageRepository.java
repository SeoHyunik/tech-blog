package com.automatic.tech_blog.repository;

import com.automatic.tech_blog.entity.TbAttachedImages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PastedImageRepository extends JpaRepository<TbAttachedImages, Long> {}
