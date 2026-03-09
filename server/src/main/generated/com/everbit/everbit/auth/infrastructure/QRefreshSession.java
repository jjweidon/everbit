package com.everbit.everbit.auth.infrastructure;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRefreshSession is a Querydsl query type for RefreshSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRefreshSession extends EntityPathBase<RefreshSession> {

    private static final long serialVersionUID = 1887387147L;

    public static final QRefreshSession refreshSession = new QRefreshSession("refreshSession");

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> expiresAt = createDateTime("expiresAt", java.time.Instant.class);

    public final StringPath jti = createString("jti");

    public final NumberPath<Long> ownerId = createNumber("ownerId", Long.class);

    public QRefreshSession(String variable) {
        super(RefreshSession.class, forVariable(variable));
    }

    public QRefreshSession(Path<? extends RefreshSession> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRefreshSession(PathMetadata metadata) {
        super(RefreshSession.class, metadata);
    }

}

