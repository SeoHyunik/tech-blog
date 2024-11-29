package com.automatic.tech_blog.repository.q_repo;

import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.entity.TbAttachedImages;
import java.util.Date;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PastedImageQRepository {
  Optional<TbAttachedImages> findByImageId(String imageId);
  String findByImageName(String imageName);

  Optional<ImageLists> findNewImages(Date since);
}
