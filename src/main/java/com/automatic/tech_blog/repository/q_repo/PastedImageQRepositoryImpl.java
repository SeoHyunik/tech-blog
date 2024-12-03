package com.automatic.tech_blog.repository.q_repo;

import com.automatic.tech_blog.dto.service.ImageInfo;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.entity.QTbAttachedImages;
import com.automatic.tech_blog.entity.TbAttachedImages;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PastedImageQRepositoryImpl implements PastedImageQRepository {
  private final JPAQueryFactory queryFactory;
  private final QTbAttachedImages tbAttachedImages = QTbAttachedImages.tbAttachedImages;

  @Override
  public Optional<TbAttachedImages> findByImageId(String imageId) {
    TbAttachedImages result = queryFactory.selectFrom(tbAttachedImages)
        .where(tbAttachedImages.imageId.eq(imageId))
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public String findByImageName(String imageName) {
    TbAttachedImages result = queryFactory.selectFrom(tbAttachedImages)
        .where(tbAttachedImages.imageName.eq(imageName))
        .fetchOne();

    return Objects.requireNonNull(result).getImageUrl();
  }

  @Override
  public Optional<ImageLists> findNewImages(Date since) {
    QTbAttachedImages tbAttachedImages = QTbAttachedImages.tbAttachedImages;

    List<ImageInfo> imageInfoList = queryFactory
        .select(Projections.constructor(
            ImageInfo.class,
            tbAttachedImages.imageId,
            tbAttachedImages.imageName,
            tbAttachedImages.imageFilePath
        ))
        .from(tbAttachedImages)
        .where(tbAttachedImages.createdAt.goe(since), tbAttachedImages.uploadedAt.isNull())
        .fetch();

    return imageInfoList.isEmpty()
        ? Optional.empty()
        : Optional.of(new ImageLists(imageInfoList));
  }

}
