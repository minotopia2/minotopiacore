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

package li.l1t.mtc.listener;

import li.l1t.mtc.ConfigHelper;
import li.l1t.mtc.MTC;
import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.clan.InvitationInfo;
import li.l1t.mtc.helper.LaterMessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Iterator;
import java.util.UUID;

public final class MainJoinListener implements Listener {
    //TODO merge in BanJoinListener
    private final MTCPlugin plugin;

    public MainJoinListener(MTCPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player plr = e.getPlayer();
        String plrName = plr.getName();
        if (ConfigHelper.isEnableTablist()) {
            char colChar = 'f';
            for (byte i = 0; i < ConfigHelper.getTabListAllowedColors().length(); i++) {
                char chr = ConfigHelper.getTabListAllowedColors().charAt(i);
                if (plr.hasPermission("mtc.tablist.color." + chr)) {
                    plr.sendMessage("Your color: §" + chr + "&" + chr);
                    colChar = chr;
                    break;
                }
            }
            if (plrName.length() <= 14) {
                plr.setPlayerListName("§" + colChar + plrName);
            } else {
                plr.setPlayerListName("§" + colChar + plrName.substring(0, 14));
            }
        }
        if (MTC.speedOnJoinPotency > 0) {
            plr.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60000, MTC.speedOnJoinPotency, false), true);
        }
        if ((ConfigHelper.isEnableItemOnJoin() && ConfigHelper.getItemOnJoin() != null)
                && !plr.getInventory().containsAtLeast(ConfigHelper.getItemOnJoin(), 1)) {
            plr.getInventory().addItem(ConfigHelper.getItemOnJoin());
        }

        //later messages
        if (LaterMessageHelper.hasMessages(plrName)) {
            LaterMessageHelper.sendMessages(plr);
        }

        //clan invitations
        if (ConfigHelper.isClanEnabled()) {
            final String str = InvitationInfo.getInvitationString(plrName, false);
            if (str != null) {
                plr.sendMessage(str);
            }
        }
        //playerhide
        if (plugin.getConfig().getBoolean("enable.playerhide", false)) {
            final Iterator<UUID> iterator = PlayerHideInteractListener.affectedPlayerIds.iterator();
            while (iterator.hasNext()) {
                final UUID targetId = iterator.next();
                final Player targetPlr = Bukkit.getPlayer(targetId);
                if (targetPlr == null) {
                    iterator.remove();
                    continue;
                }
                plr.hidePlayer(targetPlr);
            }
        }
    }
}
