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

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imageFilePath = createString("imageFilePath");

    public final StringPath imageId = createString("imageId");

    public final StringPath imageName = createString("imageName");

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath parentFileId = createString("parentFileId");

    public final DateTimePath<java.util.Date> uploadedAt = createDateTime("uploadedAt", java.util.Date.class);

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

