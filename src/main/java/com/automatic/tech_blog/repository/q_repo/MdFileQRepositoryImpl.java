package com.automatic.tech_blog.repository.q_repo;

import com.automatic.tech_blog.dto.service.FileInfo;
import com.automatic.tech_blog.entity.QTbMdFiles;
import com.automatic.tech_blog.entity.TbMdFiles;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MdFileQRepositoryImpl implements MdFileQRepository {
  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<TbMdFiles> findByFileId(String fileId) {
    QTbMdFiles tbMdFiles = QTbMdFiles.tbMdFiles;

    TbMdFiles result = queryFactory.selectFrom(tbMdFiles)
        .where(tbMdFiles.fileId.eq(fileId))
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<FileInfo> findNewFiles(Date since) {
    QTbMdFiles tbMdFiles = QTbMdFiles.tbMdFiles;

    return queryFactory
        .select(Projections.constructor(
            FileInfo.class,
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

  @Override
  public String findFileIdByFileName(String fileName) {
    QTbMdFiles tbMdFiles = QTbMdFiles.tbMdFiles;

    return queryFactory.select(tbMdFiles.fileId)
        .from(tbMdFiles)
        .where(tbMdFiles.fileName.eq(fileName))
        .fetchOne();
  }

  @Override
  public List<FileInfo> findAllFiles() {
    QTbMdFiles tbMdFiles = QTbMdFiles.tbMdFiles;

    return queryFactory
        .select(Projections.constructor(
            FileInfo.class,
            tbMdFiles.fileName,
            tbMdFiles.fileId,
            tbMdFiles.filePath,
            tbMdFiles.createdAt,
            tbMdFiles.modifiedAt,
            tbMdFiles.deletedAt
        ))
        .from(tbMdFiles)
        .fetch();
  }
}
