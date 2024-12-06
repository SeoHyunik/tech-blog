package com.automatic.tech_blog.repository;

import com.automatic.tech_blog.entity.TbTokenUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenUsageRepository extends JpaRepository<TbTokenUsage, Long> {}
