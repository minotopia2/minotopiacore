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

package li.l1t.mtc.chat.cmdspy;

import li.l1t.common.util.CommandHelper;
import li.l1t.common.util.StringHelper;
import li.l1t.mtc.MTC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Stream;


public class CommandCmdSpy implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmdspy", label)) {
            return true;
        }
        if (CommandHelper.kickConsoleFromMethod(sender, label)) {
            return true;
        }

        Player plr = (Player) sender;

        if (args.length > 0 && !args[0].equalsIgnoreCase("help")) {
            if (args.length > 1) { //If we have enough args, check sub-commands that require an argument
                switch (args[0].toLowerCase()) {
                    case "-i": //Target is a command
                        sender.sendMessage(MTC.chatPrefix + "CommandSpy -i " +
                                enabledString(
                                        CommandSpyFilters.toggleStringFilter(
                                                StringHelper.varArgsString(args, 1, false), plr
                                        )) +
                                "!");
                        return true;
                    case "-p": //Target is a player
                        @SuppressWarnings("deprecation")
                        Player target = Bukkit.getPlayerExact(args[1]); //Intended!
                        if (target == null) {
                            sender.sendMessage(MTC.chatPrefix + "Sorry, dieser Spieler ist nicht online :/");
                        } else {
                            sender.sendMessage(MTC.chatPrefix + "CommandSpy -p " +
                                    enabledString(
                                            CommandSpyFilters.togglePlayerFilter(
                                                    target.getUniqueId(), plr
                                            )) +
                                    "!");
                        }
                        return true;
                } //So we haven't matched any of the sub-commands requiring arguments.
            }
            //Well, we still have those without!
            switch (args[0].toLowerCase()) {
                case "-c": //Clear all filters (that can be cleared)
                    CommandSpyFilters.unsubscribeFromAll(plr.getUniqueId());
                    sender.sendMessage(MTC.chatPrefix + "Alle Filter deaktiviert!");
                    return true;
                case "-l": //List filters
                    return printFilters(sender, CommandSpyFilters.getSubscribedFilters(plr.getUniqueId()));
                case "-la":
                    return printFilters(sender, CommandSpyFilters.getActiveFilters().stream());
                case "-a": //Target all commands
                    sender.sendMessage(MTC.chatPrefix + "CommandSpy -a " +
                            enabledString(
                                    CommandSpyFilters.toggleGlobalFilter(
                                            plr
                                    )) +
                            "!");
                    return true;
                case "--regex-help":
                    sender.sendMessage("§eDu kannst bei -i reguläre Ausdrücke verwenden, wenn du vor dem Input §6!r§e verwendest. " +
                            "(§6/cmdspy -i !r[Deine RegEx]§e) Die RegEx wird eingeschlossen in §6([Deine RegEx])\\s*§e und die erste Capturing Group " +
                            "wird dem Benutzer angezeigt. Um dein eigenes Output zu definieren, verwende ?: vor der RegEx.");
                    return true;
            }
        }

        return this.printHelpTo(sender);
    }

    private boolean printFilters(CommandSender sender, Stream<CommandSpyFilter> filters) {
        int filterAmount = filters
                .mapToInt(filter -> {
                    sender.sendMessage(filter.niceRepresentation()); //This is where they get the info about the filter
                    return 1;
                }).sum();

        sender.sendMessage(MTC.chatPrefix + "Du hast " + (filterAmount == 0 ? "keine" : filterAmount) + " Filter abonniert.");
        return true;
    }

    public String enabledString(boolean enabled) {
        return (enabled ? "" : "de") + "aktiviert";
    }

    public boolean printHelpTo(CommandSender sender) {
        sender.sendMessage("§e/cmdspy -a §6Aktiviert CommandSpy für alle Befehle.");
        sender.sendMessage("§e/cmdspy -i <CMD> §6Aktiviert CommandSpy für einen einzelnen Befehl.");
        sender.sendMessage("§e/cmdspy -p <SPIELER> §6Aktiviert CommandSpy für einen bestimmten Spieler.");
        sender.sendMessage("§e/cmdspy -c §6Deaktiviert CommandSpy");
        sender.sendMessage("§e/cmdspy -l §6Zeigt alle aktivierten Filter an.");
        sender.sendMessage("§e/cmdspy --regex-help §6Verwendung von RegEx");
        sender.sendMessage("§c-i akzeptiert (nur) Befehle ohne Slash.");
        return true;
    }
}
