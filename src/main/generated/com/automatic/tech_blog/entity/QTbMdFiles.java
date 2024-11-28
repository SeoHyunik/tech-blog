package com.automatic.tech_blog.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTbMdFiles is a Querydsl query type for TbMdFiles
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTbMdFiles extends EntityPathBase<TbMdFiles> {

    private static final long serialVersionUID = 280502842L;

    public static final QTbMdFiles tbMdFiles = new QTbMdFiles("tbMdFiles");

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final DateTimePath<java.util.Date> deletedAt = createDateTime("deletedAt", java.util.Date.class);

    public final StringPath fileId = createString("fileId");

    public final StringPath fileName = createString("fileName");

    public final StringPath filePath = createString("filePath");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.util.Date> modifiedAt = createDateTime("modifiedAt", java.util.Date.class);

    public QTbMdFiles(String variable) {
        super(TbMdFiles.class, forVariable(variable));
    }

    public QTbMdFiles(Path<? extends TbMdFiles> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTbMdFiles(PathMetadata metadata) {
        super(TbMdFiles.class, metadata);
    }

}

