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

package li.l1t.mtc.module.vote.sql.queue;

import li.l1t.common.sql.sane.util.AbstractJdbcEntityCreator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Creates the unique id of queued votes from JDBC result sets.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-12-28
 */
public class QueuedVoteIdCreator extends AbstractJdbcEntityCreator<UUID> {
    @Override
    public UUID createFromCurrentRow(ResultSet rs) throws SQLException {
        return uuid(rs, "vote_id");
    }
}
