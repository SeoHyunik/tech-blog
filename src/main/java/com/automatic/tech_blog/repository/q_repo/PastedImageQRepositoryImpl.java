package com.automatic.tech_blog.repository.q_repo;

import com.automatic.tech_blog.entity.QTbAttachedImages;
import com.automatic.tech_blog.entity.TbAttachedImages;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PastedImageQRepositoryImpl implements PastedImageQRepository {
  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<TbAttachedImages> findByImageId(String imageId) {
    QTbAttachedImages tbAttachedImages = QTbAttachedImages.tbAttachedImages;

    TbAttachedImages result = queryFactory.selectFrom(tbAttachedImages)
        .where(tbAttachedImages.imageId.eq(imageId))
        .fetchOne();

    return Optional.ofNullable(result);
  }
}
