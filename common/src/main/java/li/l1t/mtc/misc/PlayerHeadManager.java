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

package li.l1t.mtc.misc;

import li.l1t.common.util.CommandHelper;
import li.l1t.mtc.helper.MTCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

public class PlayerHeadManager {

    public String[] args;
    public String label;

    public PlayerHeadManager(String[] args, String label) {
        this.args = args;
        this.label = label;
    }

    public static ItemMeta setSkullOwner(SkullMeta meta, String owner) {
        meta.setOwner(owner);
        return meta;
    }

    private static boolean kickConsoleFromMethod(CommandSender sender, String label) {
        return CommandHelper.kickConsoleFromMethod(sender, label);
    }

    public boolean getAllHead(CommandSender sender) {
        if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.playerhead.getall." + this.args[1], this.label)) {
            return true;
        }

        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        head.setItemMeta(PlayerHeadManager.setSkullOwner((SkullMeta) head.getItemMeta(), this.args[1]));
        for (Player plr : Bukkit.getOnlinePlayers()) {
            Map<Integer, ItemStack> rtrn = plr.getInventory().addItem(head);
            if (rtrn.size() != 0) {
                plr.sendMessage("§7Du hättest den Kopf von §3" + this.args[1] + " §7bekommen, wenn du Platz in deinem Inventar gehabt hättest :(");
            }
        }
        sender.sendMessage("§7Alle Spieler besitzen nun den Kopf von §3" + this.args[1] + "§7.");
        if (MTCHelper.isEnabled(".command.playerhead.getall-op-notify")) {
            CommandHelper.sendImportantActionMessage(sender, "Giving §3" + this.args[1] + "§a§o's head to all players");
        }

        return true;
    }

    public boolean getHead(CommandSender sender) {
        if (PlayerHeadManager.kickConsoleFromMethod(sender, this.label)) {
            return true;
        }
        if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.playerhead.get." + this.args[1], this.label)) {
            return true;
        }

        Player plr = (Player) sender;
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        head.setItemMeta(PlayerHeadManager.setSkullOwner((SkullMeta) head.getItemMeta(), this.args[1]));
        Map<Integer, ItemStack> rtrn = plr.getInventory().addItem(head);
        if (rtrn.size() != 0) {
            plr.sendMessage("§7Dein Inventar ist voll! Bitte schaffe Platz und versuche es erneut!");
            return true;
        }
        plr.sendMessage("§7Du besitzt nun den Kopf von §3" + this.args[1] + "§7! Viel Spass damit :)");

        return true;
    }
    //UTIL METHODS

    public boolean setHead(CommandSender sender) {
        if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.playerhead.set." + this.args[1], this.label)) {
            return true;
        }
        if (PlayerHeadManager.kickConsoleFromMethod(sender, this.label)) {
            return true;
        }

        Player plr = (Player) sender;
        ItemStack head = plr.getItemInHand();
        if (head.getType() != Material.SKULL_ITEM) {
            plr.sendMessage("§7Du musst einen Spielerkopf in der Hand halten!");
            return true;
        }
        head.setItemMeta(PlayerHeadManager.setSkullOwner((SkullMeta) head.getItemMeta(), this.args[1]));
        plr.sendMessage("§7Du besitzt nun den Kopf von §3" + this.args[1] + "§7! Viel Spass damit :)");

        return true;
    }
}
