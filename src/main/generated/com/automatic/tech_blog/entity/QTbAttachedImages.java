package com.automatic.tech_blog.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTbAttachedImages is a Querydsl query type for TbAttachedImages
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTbAttachedImages extends EntityPathBase<TbAttachedImages> {

    private static final long serialVersionUID = 1382633698L;

    public static final QTbAttachedImages tbAttachedImages = new QTbAttachedImages("tbAttachedImages");

    public final NumberPath<Integer> fileId = createNumber("fileId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imagePath = createString("imagePath");

    public final DateTimePath<java.sql.Timestamp> uploadedAt = createDateTime("uploadedAt", java.sql.Timestamp.class);

    public final StringPath wpUrl = createString("wpUrl");

    public QTbAttachedImages(String variable) {
        super(TbAttachedImages.class, forVariable(variable));
    }

    public QTbAttachedImages(Path<? extends TbAttachedImages> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTbAttachedImages(PathMetadata metadata) {
        super(TbAttachedImages.class, metadata);
    }

}

