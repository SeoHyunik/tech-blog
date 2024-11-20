package com.automatic.tech_blog.repository;

import com.automatic.tech_blog.entity.TbPostsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostedArticleRepository extends JpaRepository<TbPostsInfo, Long> {}
