/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.misc.cmd;

import li.l1t.common.misc.HelpManager;
import li.l1t.mtc.helper.MTCHelper;
import li.l1t.mtc.misc.PlayerHeadManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public final class CommandPlayerHead implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!MTCHelper.isEnabledAndMsg(".command.playerhead.command", sender)) {
            return true;
        }
        //permissions handeled in Manager
        if (args.length > 0) {
            switch (args[0]) {
                case "get":
                    PlayerHeadManager phm = new PlayerHeadManager(args, label);
                    phm.getHead(sender);
                    break;
                case "set":
                    PlayerHeadManager phm1 = new PlayerHeadManager(args, label);
                    phm1.setHead(sender);
                    break;
                case "getall":
                    PlayerHeadManager phm2 = new PlayerHeadManager(args, label);
                    phm2.getAllHead(sender);
                    break;
                default:
                    sender.sendMessage("§8Unbekannte Aktion. Versuche §3get§8 oder §3set§8.");
                    HelpManager.tryPrintHelp("ph", sender, label, "", "mts help ph");
            }
        } else {
            HelpManager.tryPrintHelp("ph", sender, label, "", "mts help ph");
        }
        return true;
    }

}