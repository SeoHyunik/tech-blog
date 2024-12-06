package com.automatic.tech_blog.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTbTokenUsage is a Querydsl query type for TbTokenUsage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTbTokenUsage extends EntityPathBase<TbTokenUsage> {

    private static final long serialVersionUID = -481945138L;

    public static final QTbTokenUsage tbTokenUsage = new QTbTokenUsage("tbTokenUsage");

    public final NumberPath<java.math.BigDecimal> convertedKrw = createNumber("convertedKrw", java.math.BigDecimal.class);

    public final StringPath fileId = createString("fileId");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> inputTokens = createNumber("inputTokens", Integer.class);

    public final StringPath model = createString("model");

    public final NumberPath<Integer> outputTokens = createNumber("outputTokens", Integer.class);

    public final NumberPath<java.math.BigDecimal> profitKrw = createNumber("profitKrw", java.math.BigDecimal.class);

    public QTbTokenUsage(String variable) {
        super(TbTokenUsage.class, forVariable(variable));
    }

    public QTbTokenUsage(Path<? extends TbTokenUsage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTbTokenUsage(PathMetadata metadata) {
        super(TbTokenUsage.class, metadata);
    }

}

