package com.automatic.tech_blog.repository.q_repo;

import com.automatic.tech_blog.entity.TbAttachedImages;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PastedImageQRepository {
  Optional<TbAttachedImages> findByImageId(String imageId);
}
