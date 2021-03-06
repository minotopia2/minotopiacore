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

package li.l1t.mtc.module.lanatus.pex;

import com.google.common.base.Verify;
import li.l1t.lanatus.api.LanatusClient;
import li.l1t.lanatus.api.account.AccountSnapshot;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.logging.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

import java.util.Collections;
import java.util.Optional;

/**
 * Listens for join events and forwards them to a {@link GroupMapping} if a new Lanatus group must
 * be applied to a player.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-11-01
 */
public class GroupChangeJoinListener implements Listener {
    private static final Logger LOGGER = LogManager.getLogger(GroupChangeJoinListener.class);
    private final GroupMapping mapping;
    private final PermissionManager pex;
    private final LanatusClient lanatus;

    public GroupChangeJoinListener(GroupMapping mapping, PermissionManager pex, LanatusClient lanatus) {
        this.mapping = mapping;
        this.pex = pex;
        this.lanatus = lanatus;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void handleJoinForGroupChange(PlayerJoinEvent evt) {
        PermissionUser user = pex.getUser(evt.getPlayer());
        if (areAllGroupsModifiable(user)) {
            Optional<AccountSnapshot> account = lanatus.accounts().find(evt.getPlayer().getUniqueId());
            if (account.isPresent()) {
                mapping.findPexGroupFor(account.get().getLastRank())
                        .ifPresent(newGroup -> setPexGroupTo(newGroup, user, evt.getPlayer()));
            }
        }
    }

    private boolean areAllGroupsModifiable(PermissionUser user) {
        return user.getOwnParents().stream().allMatch(this::isModificationAllowed);
    }

    private boolean isModificationAllowed(PermissionGroup group) {
        return group.getOwnOptionBoolean(LanatusPexModule.AUTORANK_OPTION_NAME, null, false);
    }

    private void setPexGroupTo(String groupName, PermissionUser user, Player player) {
        PermissionGroup group = pex.getGroup(groupName);
        Verify.verifyNotNull(group, "PEx shouldn't return null group", groupName, user);
        LOGGER.info("LanatusPexModule: Changing group for {} {} from {} to {}",
                user.getName(), user.getIdentifier(), user.getOwnParentIdentifiers(), groupName);
        user.setParents(Collections.singletonList(group));
        MessageType.RESULT_LINE.sendTo(player, "Deine Gruppe ist jetzt §s%s§p.", groupName);
    }
}
