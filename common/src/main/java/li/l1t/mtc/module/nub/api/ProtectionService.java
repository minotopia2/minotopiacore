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

package li.l1t.mtc.module.nub.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A service that allows to manage N.u.b. protections, emitting messages to notify players of modifications.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-12-08
 */
public interface ProtectionService {
    /**
     * Protects given player for the default duration and saves that protection to the database. Also notifies the
     * player of the changes.
     *
     * @param player the player to protect
     */
    void startProtection(Player player);

    /**
     * Immediately cancels given player's protection, if any, and removes if from the database. This does not allow
     * players to resume their protection. Also notifies the player whether their protection was cancelled.
     *
     * @param player the player whose protection to cancel
     *
     * @return whether the player was protected
     */
    boolean cancelProtection(Player player);

    /**
     * Pauses the protection of given player, saving the current amount of time left to the database, so that it may be
     * resumed later. Also notifies the player of the change.
     *
     * @param player the player whose protection to pause
     */
    void pauseProtection(Player player);

    /**
     * Resumes the paused protection of given player. Also notifies them of the change.
     *
     * @param player the player whose protection to resume
     *
     * @throws NoSuchProtectionException if given player does not have a paused protection
     */
    void resumeProtection(Player player) throws NoSuchProtectionException;

    /**
     * Resumes given player's protection if they have a paused protection, or otherwise starts a protection on them.
     *
     * @param player the player to operate on
     *
     * @see #startProtection(Player)
     * @see #resumeProtection(Player)
     */
    void startOrResumeProtection(Player player);

    /**
     * @param player the player whose protection to check
     *
     * @return whether given player is currently protected locally, not querying the database
     */
    boolean hasProtection(Player player);

    /**
     * @param player the player to examine
     *
     * @return whether given player is eligible for protection either through a paused protection or through not having
     * played on the server before
     */
    boolean isEligibleForProtection(Player player);

    /**
     * Expires given protection, notifying given player of the event.
     *
     * @param player     the player to notify
     * @param protection the protection to expire
     *
     * @throws IllegalArgumentException if given protection does not belong to given player or given protection has not
     *                                  expired yet
     */
    void expireProtection(Player player, NubProtection protection);

    void showProtectionStatusTo(CommandSender sender, UUID playerId);

    void showOwnProtectionStatusTo(Player player);
}
