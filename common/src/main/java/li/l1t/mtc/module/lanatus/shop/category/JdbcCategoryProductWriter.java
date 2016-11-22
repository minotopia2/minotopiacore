/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.lanatus.shop.category;

import li.l1t.common.sql.sane.AbstractSqlConnected;
import li.l1t.common.sql.sane.SaneSql;
import li.l1t.lanatus.api.product.Product;
import li.l1t.lanatus.shop.api.Category;

/**
 * Creates and deletes category-product associations.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-22-11
 */
public class JdbcCategoryProductWriter extends AbstractSqlConnected {
    protected JdbcCategoryProductWriter(SaneSql saneSql) {
        super(saneSql);
    }

    public void createAssociation(Category category, Product product) {
        sql().updateRaw(
                "INSERT IGNORE INTO " + SqlCategoryRepository.PRODUCT_MAPPING_TABLE_NAME + " " +
                        "SET category_id=?, product_id=?",
                category.getUniqueId().toString(), product.getUniqueId().toString()
        );
    }

    public void removeAssociation(Category category, Product product) {
        sql().updateRaw(
                "DELETE FROM "+SqlCategoryRepository.PRODUCT_MAPPING_TABLE_NAME+" " +
                        "WHERE category_id=? AND product_id=?",
                category.getUniqueId().toString(), product.getUniqueId().toString()
        );
    }
}
