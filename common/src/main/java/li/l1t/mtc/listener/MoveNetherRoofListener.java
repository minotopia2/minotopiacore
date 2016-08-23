/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.listener;

import li.l1t.mtc.MTC;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;


public final class MoveNetherRoofListener implements Listener {
    private final MTC plugin;

    public MoveNetherRoofListener(MTC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNetherMove(PlayerMoveEvent e) {
        if (!e.getTo().getWorld().getEnvironment().equals(Environment.NETHER)) {
            return;
        }
        if (e.getTo().getBlockY() > 125) {
            if (e.getPlayer().hasPermission("mtc.ignore")) {
                return;
            }
            e.getPlayer().sendMessage(MTC.chatPrefix + "§eDu darfst nicht über das Netherdach :)");
            e.getPlayer().sendMessage(MTC.chatPrefix + "§eViel Spaß beim Spawn.");
            e.getPlayer().sendMessage(MTC.chatPrefix + "§eDeine Koordinaten: §a" + e.getPlayer().getLocation().toString());
            e.getPlayer().sendMessage(MTC.chatPrefix + "§eWenn du mit dieser automatisierten Entscheidung unzufrieden bist, wende dich " +
                    "bitte mit Screenshot dieser Nachricht an das Team! (Forum!)");

            if (plugin.getXLoginHook().getSpawnLocation() == null) {
                e.setTo(e.getFrom());
            } else {
                e.getPlayer().teleport(plugin.getXLoginHook().getSpawnLocation());
            }
        }
    }
}