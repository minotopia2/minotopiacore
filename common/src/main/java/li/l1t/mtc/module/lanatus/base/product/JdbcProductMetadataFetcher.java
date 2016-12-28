/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.lanatus.base.product;

import li.l1t.common.exception.DatabaseException;
import li.l1t.common.sql.sane.SaneSql;
import li.l1t.common.sql.sane.result.QueryResult;
import li.l1t.common.sql.sane.util.AbstractJdbcFetcher;
import li.l1t.common.sql.sane.util.JdbcEntityCreator;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-12-05
 */
public abstract class JdbcProductMetadataFetcher<T extends ProductMetadata> extends AbstractJdbcFetcher<T> {
    public JdbcProductMetadataFetcher(JdbcEntityCreator<? extends T> creator, SaneSql saneSql) {
        super(creator, saneSql);
    }

    public Optional<T> findByProduct(UUID productId) {
        try (QueryResult result = selectByProductId(productId)) {
            if(result.rs().next()) {
                return Optional.of(creator.createFromCurrentRow(result.rs()));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw DatabaseException.wrap(e);
        }
    }

    private QueryResult selectByProductId(UUID productId) {
        return select("WHERE product_id = ?", productId.toString());
    }
}
