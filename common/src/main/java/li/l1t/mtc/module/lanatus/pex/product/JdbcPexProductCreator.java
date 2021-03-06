/*
 * MinoTopiaCore
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.l1t.mtc.module.lanatus.pex.product;

import li.l1t.mtc.module.lanatus.base.product.JdbcProductMetadataCreator;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Creates instances of pex product metadata from JDBC ResultSet objects.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-25-11
 */
class JdbcPexProductCreator extends JdbcProductMetadataCreator<PexProduct> {
    @Override
    public PexProduct createFromCurrentRow(ResultSet rs) throws SQLException {
        return new SqlPexProduct(
                productId(rs), commaSeparated(rs, "commands"),
                rs.getString("sourcerank"), rs.getString("targetrank")
        );
    }
}
