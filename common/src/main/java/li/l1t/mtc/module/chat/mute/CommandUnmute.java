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

package li.l1t.mtc.module.chat.mute;

import li.l1t.common.exception.UserException;
import li.l1t.common.util.CommandHelper;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.hook.XLoginHook;
import li.l1t.mtc.module.chat.mute.api.MuteManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * A command to toggle mute, with an optional reason.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-08-21
 */
class CommandUnmute implements CommandExecutor {
    private final XLoginHook xLoginHook;
    private final MuteManager muteManager;

    CommandUnmute(XLoginHook xLoginHook, MuteManager muteManager) {
        this.xLoginHook = xLoginHook;
        this.muteManager = muteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
            sendHelpTo(sender);
            return true;
        }
        XLoginHook.Profile profile = findSingleMatchingProfileOrFail(args[0], sender);
        boolean wasMuted = muteManager.removeMute(profile);
        if (!wasMuted) {
            throw new UserException("%s ist nicht gemuted.", profile.getName());
        }
        notifySender(sender, profile);
        notifyAllPlayers(profile, sender);
        notifyTeamMembers(sender, profile);
        return true;
    }

    private void sendHelpTo(CommandSender sender) {
        sender.sendMessage("§a/unmute [Spieler] §6Entmuted einen Spieler");
    }

    private XLoginHook.Profile findSingleMatchingProfileOrFail(String input, CommandSender sender) {
        return xLoginHook.findSingleMatchingProfileOrFail(
                input, sender, profile -> "/unmute " + profile.getUniqueId()
        );
    }

    private void notifySender(CommandSender sender, XLoginHook.Profile profile) {
        MessageType.RESULT_LINE_SUCCESS.sendTo(sender, "Du hast %s entmuted.", profile.getName());
    }

    private void notifyAllPlayers(XLoginHook.Profile profile, CommandSender sender) {
        MessageType.BROADCAST.broadcast(sender.getServer(),
                "§s%s §pist jetzt nicht mehr gemuted.", profile.getName()
        );
    }

    private void notifyTeamMembers(CommandSender sender, XLoginHook.Profile profile) {
        CommandHelper.broadcast(
                MessageType.BROADCAST.format(
                        "§s%s§p wurde entmuted von §s%s§p.",
                        profile.getName(), sender.getName()
                ), "mtc.spy"
        );
    }
}
