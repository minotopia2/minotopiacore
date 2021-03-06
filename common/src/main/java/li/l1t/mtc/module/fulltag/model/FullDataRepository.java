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

package li.l1t.mtc.module.fulltag.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import li.l1t.common.sql.QueryResult;
import li.l1t.common.sql.SpigotSql;
import li.l1t.common.sql.UpdateResult;
import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.misc.CacheHelper;
import li.l1t.mtc.module.fulltag.FullTagModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Repository class for {@link FullData}, retrieving data from an underlying MySQL database.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/08/15
 */
public class FullDataRepository implements li.l1t.mtc.api.misc.Cache {
    public static final String TABLE_NAME = "mt_main.fulldata";

    private final SpigotSql sql;
    @Nonnull
    private final FullTagModule module;
    @Nonnull
    private Cache<Integer, FullData> dataCache = CacheBuilder.newBuilder()
            .expireAfterAccess(40, TimeUnit.MINUTES)
            .removalListener((RemovalNotification<Integer, FullData> notification) -> {
                if (notification.getValue() != null) {
                    updateComment(notification.getValue());
                    notification.getValue().setValid(false);
                }
            })
            .build();

    public FullDataRepository(@Nonnull FullTagModule module) {
        this.module = module;
        this.sql = module.getPlugin().getSql();
        CacheHelper.registerCache(this);
    }

    /**
     * Attempts to find a {@link FullData} instance from the underlying database by its id, or
     * returns a cached instance, if available.
     *
     * @param id the id to look for
     * @return a {@link FullData} instance or null if there is no such instance
     * @throws IllegalStateException if the data in the database is invalid or could not be
     *                               retrieved
     */
    @Nullable
    public FullData getById(int id) throws IllegalStateException {
        FullData data = dataCache.getIfPresent(id);
        if (data == null) {
            data = findById(id);
            if (data != null) {
                dataCache.put(id, data);
            }
        }
        return data;
    }

    /**
     * Attempts to find a {@link FullData} instance from the underlying database by its id.
     *
     * @param id the id to look for
     * @return a {@link FullData} instance or null if there is no such instance
     * @throws IllegalStateException if the data in the database is invalid or could not be
     *                               retrieved
     */
    private FullData findById(int id) throws IllegalStateException {
        return findByWhere("WHERE id=?", "by id " + id, id).stream()
                .findFirst().orElse(null);
    }

    /**
     * Attempts to find a list of {@link FullData} instances by a receiver player's unique id.
     *
     * @param receiverId the unique id to look for
     * @return an immutable list containing the found data, if any
     * @throws IllegalStateException if a database error occurs
     */
    public List<FullData> findByReceiver(@Nonnull UUID receiverId) throws IllegalStateException {
        return findByWhere("WHERE receiver_id=?", "by receiver " + receiverId, receiverId.toString());
    }

    public List<FullData> findNotInRegistry(UUID receiverId) {
        return findByWhere("d LEFT JOIN "+FullRegistry.TABLE_NAME+" r " +
                "ON d.id = r.full_id " +
                "WHERE d.receiver_id = ? AND r.full_id IS NULL",
                "not in registry", receiverId.toString());
    }

    private List<FullData> findByWhere(String whereClause, String desc, Object... args) throws IllegalStateException {
        try (QueryResult qr = sql.executeQueryWithResult("SELECT * FROM " + TABLE_NAME + " " + whereClause, args)) {
            ResultSet rs = qr.rs();
            if (!rs.next()) {
                return ImmutableList.of();
            }

            List<FullData> result = new ArrayList<>();
            do {
                result.add(new FullData(rs.getInt(1), rs.getTimestamp(2).toLocalDateTime(),
                        rs.getString(3), UUID.fromString(rs.getString(4)), UUID.fromString(rs.getString(5)),
                        FullPart.values()[rs.getInt(6)], rs.getBoolean(7),
                        UUID.fromString(rs.getString(8))));
            } while (rs.next());

            return Collections.unmodifiableList(result);
        } catch (SQLException e) {
            throw new IllegalStateException(String.format(
                    "Could not retrieve FullData %s because of a database error: %d: %s",
                    desc, e.getErrorCode(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format(
                    "Invalid UUID in database for FullData %s:%s",
                    desc, e.getMessage()));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException(String.format(
                    "Invalid full part id in database for FullData %s",
                    desc));
        }
    }

    /**
     * Creates a new full data and inserts it into the database.
     *
     * @param comment    the comment string to attach
     * @param senderId   the unique id of the player who created the full item
     * @param receiverId the unique id of the player who owns the full item
     * @param part       the full part the item represents
     * @param thorns     whether the full item has the Thorns enchantment
     * @return the created full data
     * @throws IllegalStateException if a database error occurs
     */
    @Nonnull
    public FullData create(String comment, @Nonnull UUID senderId, @Nonnull UUID receiverId, @Nonnull FullPart part, boolean thorns) {
        try (UpdateResult ur = sql.executeUpdateWithGenKeys(
                "INSERT INTO " + TABLE_NAME + " SET comment=?,sender_id=?,receiver_id=?,part_id=?,thorns=?",
                comment, senderId.toString(), receiverId.toString(), part.ordinal(), thorns
        )) {
            ur.vouchForGeneratedKeys();
            ur.gk().next();
            return new FullData(
                    ur.gk().getInt(1), LocalDateTime.now(),
                    comment, senderId, receiverId, part, thorns,
                    null);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Updates the comment of a full item's metadata in the database if it has been modified
     * locally.
     *
     * @param data the target data object
     */
    private void updateComment(@Nonnull FullData data) {
        if (data.isModified()) {
            data.resetModified();
            if (module.getPlugin().isEnabled()) {
                sql.executeSimpleUpdateAsync("UPDATE " + TABLE_NAME + " SET comment=? WHERE id=?",
                        data.getComment(), data.getId());
            } else { //When we're disabling, we can't register tasks. Sad, but true.
                sql.safelyExecuteUpdate("UPDATE " + TABLE_NAME + " SET comment=? WHERE id=?",
                        data.getComment(), data.getId());
            }
        }
    }

    /**
     * Flushes this repository's cache, writing all pending changes to database and removing expired
     * entries.
     */
    @Override
    public void clearCache(boolean forced, MTCPlugin plugin) {
        dataCache.cleanUp();
        dataCache.asMap().values().forEach(this::updateComment);
    }
}
