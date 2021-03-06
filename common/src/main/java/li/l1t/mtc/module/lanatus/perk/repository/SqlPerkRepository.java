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

package li.l1t.mtc.module.lanatus.perk.repository;

import li.l1t.common.collections.cache.GuavaMapCache;
import li.l1t.common.collections.cache.MapCache;
import li.l1t.common.collections.cache.OptionalCache;
import li.l1t.common.collections.cache.OptionalGuavaCache;
import li.l1t.common.sql.sane.SaneSql;
import li.l1t.lanatus.api.LanatusClient;
import li.l1t.mtc.api.module.inject.InjectMe;
import li.l1t.mtc.module.lanatus.base.MTCLanatusClient;
import li.l1t.mtc.module.lanatus.perk.api.PerkRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repository of perk metadata.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-12-06
 */
public class SqlPerkRepository implements PerkRepository {
    public static final String TABLE_NAME = "mt_main.lanatus_perk_product";
    public static final String AVAILABLE_TABLE_NAME = "mt_main.lanatus_perk_available";
    public static final String ENABLED_TABLE_NAME = "mt_main.lanatus_perk_enabled";
    private final OptionalCache<UUID, PerkMeta> idMetaCache = new OptionalGuavaCache<>();
    private final MapCache<UUID, AvailablePerksSet> playerAvailablePerksCache = new GuavaMapCache<>();
    private final MapCache<UUID, Collection<PerkMeta>> playerEnabledPerksCache = new GuavaMapCache<>();
    private final JdbcPerkMetaFetcher perkMetaFetcher;
    private final JdbcAvailablePerksFetcher availablePerksFetcher;
    private final JdbcEnabledPerksFetcher enabledPerksFetcher;
    private final JdbcAvailablePerksWriter availablePerksWriter;
    private final JdbcEnabledPerksWriter enabledPerksWriter;
    private final LanatusClient client;

    @InjectMe
    public SqlPerkRepository(MTCLanatusClient client, SaneSql sql) {
        this.client = client;
        this.perkMetaFetcher = new JdbcPerkMetaFetcher(new JdbcPerkMetaCreator(), sql);
        this.availablePerksFetcher = new JdbcAvailablePerksFetcher(new JdbcAvailablePerkCreator(), sql);
        this.enabledPerksFetcher = new JdbcEnabledPerksFetcher(sql);
        this.availablePerksWriter = new JdbcAvailablePerksWriter(sql);
        this.enabledPerksWriter = new JdbcEnabledPerksWriter(sql);
    }

    @Override
    public Optional<PerkMeta> findByProductId(UUID productId) {
        return idMetaCache.getOrCompute(productId, perkMetaFetcher::findByProduct);
    }

    @Override
    public AvailablePerksSet findAvailableByPlayerId(UUID playerId) {
        return playerAvailablePerksCache.getOrCompute(playerId, availablePerksFetcher::findByPlayerId);
    }

    @Override
    public boolean isPerkAvailable(UUID playerId, UUID perkId) {
        return findAvailableByPlayerId(playerId).stream()
                .map(AvailablePerk::getProductId)
                .anyMatch(perkId::equals);
    }

    @Override
    public Collection<PerkMeta> findEnabledByPlayerId(UUID playerId) {
        return playerEnabledPerksCache.getOrCompute(playerId, this::fetchEnabledPerks);
    }

    @Override
    public boolean isPerkEnabled(UUID playerId, PerkMeta perk) {
        return findEnabledByPlayerId(playerId).contains(perk);
    }

    @Override
    public boolean isPerkEnabled(UUID playerId, UUID perkId) {
        if (playerEnabledPerksCache.containsKey(playerId)) {
            return playerEnabledPerksCache.get(playerId)
                    .orElseThrow(IllegalStateException::new)
                    .stream()
                    .anyMatch(perk -> perk.getProductId().equals(perkId));
        } else {
            Collection<UUID> perkIds = enabledPerksFetcher.getEnabledPerksByPlayerId(playerId);
            playerEnabledPerksCache.cache(playerId, mapToMetas(perkIds));
            return perkIds.contains(perkId);
        }
    }

    private Collection<PerkMeta> fetchEnabledPerks(UUID playerId) {
        return mapToMetas(enabledPerksFetcher.getEnabledPerksByPlayerId(playerId));
    }

    private Collection<PerkMeta> mapToMetas(Collection<UUID> perkIds) {
        return perkIds.stream()
                .map(this::findByProductId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @Override
    public void enablePlayerPerk(UUID playerId, UUID perkId) {
        enabledPerksWriter.enablePlayerPerk(playerId, perkId);
        playerEnabledPerksCache.invalidateKey(playerId);
    }

    @Override
    public void disablePlayerPerk(UUID playerId, UUID perkId) {
        enabledPerksWriter.disablePlayerPerk(playerId, perkId);
        playerEnabledPerksCache.invalidateKey(playerId);
    }

    @Override
    public void makeAvailablePermanently(UUID playerId, UUID perkId) {
        availablePerksWriter.makeAvailablePermanently(playerId, perkId);
        playerAvailablePerksCache.invalidateKey(playerId);
    }

    @Override
    public void makeAvailableUntil(UUID playerId, UUID perkId, Instant expiryTime) {
        availablePerksWriter.makeAvailableUntil(playerId, perkId, expiryTime);
        playerAvailablePerksCache.invalidateKey(playerId);
    }

    @Override
    public void clearCache() {
        idMetaCache.clear();
        playerAvailablePerksCache.clear();
        playerEnabledPerksCache.clear();
    }

    @Override
    public void clearCachesFor(UUID playerId) {
        playerAvailablePerksCache.invalidateKey(playerId);
        playerEnabledPerksCache.invalidateKey(playerId);
    }

    @Override
    public LanatusClient client() {
        return client;
    }
}
