package com.automatic.tech_blog.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTbPostsInfo is a Querydsl query type for TbPostsInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTbPostsInfo extends EntityPathBase<TbPostsInfo> {

    private static final long serialVersionUID = 1447449531L;

    public static final QTbPostsInfo tbPostsInfo = new QTbPostsInfo("tbPostsInfo");

    public final NumberPath<Integer> contentId = createNumber("contentId", Integer.class);

    public final StringPath fileId = createString("fileId");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.util.Date> publishedAt = createDateTime("publishedAt", java.util.Date.class);

    public final StringPath status = createString("status");

    public final StringPath title = createString("title");

    public QTbPostsInfo(String variable) {
        super(TbPostsInfo.class, forVariable(variable));
    }

    public QTbPostsInfo(Path<? extends TbPostsInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTbPostsInfo(PathMetadata metadata) {
        super(TbPostsInfo.class, metadata);
    }

}

