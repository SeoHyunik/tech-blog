package com.automatic.tech_blog.repository.q_repo;

import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.entity.QTbMdFiles;
import com.automatic.tech_blog.entity.TbMdFiles;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MdFileQRepoImpl implements MdFileQRepo{

  private final JPAQueryFactory queryFactory;

  @Override
  public List<MdFileInfo> findNewFiles(Date since) {
    QTbMdFiles tbMdFiles = QTbMdFiles.tbMdFiles;

    return queryFactory
        .select(Projections.constructor(
            MdFileInfo.class,
            tbMdFiles.fileName,
            tbMdFiles.fileId,
            tbMdFiles.filePath,
            tbMdFiles.createdAt,
            tbMdFiles.modifiedAt,
            tbMdFiles.deletedAt
        ))
        .from(tbMdFiles)
        .where(tbMdFiles.createdAt.goe(since).or(tbMdFiles.modifiedAt.goe(since)))
        .fetch();
  }
}
