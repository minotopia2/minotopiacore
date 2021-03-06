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

package li.l1t.mtc.misc.cmd;

import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.common.misc.HelpManager;
import li.l1t.common.util.CommandHelper;
import li.l1t.common.util.StringHelper;
import li.l1t.common.util.inventory.InventoryHelper;
import li.l1t.mtc.MTC;
import li.l1t.mtc.chat.MTCChatHelper;
import li.l1t.mtc.clan.ClanHelper;
import li.l1t.mtc.cron.RunnableCronjob5Minutes;
import li.l1t.mtc.helper.MTCHelper;
import li.l1t.mtc.misc.CacheHelper;
import li.l1t.mtc.misc.ClearCacheEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CommandMTC extends MTCCommandExecutor {

    private final MTC plugin;

    public CommandMTC(MTC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean catchCommand(CommandSender sender, String senderName, Command cmd, String label, String[] args) {
        if (!MTCHelper.isEnabledAndMsg(".command.mtc", sender, plugin)) {
            return true;
        }
        if (args.length == 0) {
            if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.credits", label)) {
                return true;
            }

            sender.sendMessage("§eMinoTopiaCore by Literallie. https://l1t.li/");
            sender.sendMessage("§9Version " + MTC.PLUGIN_VERSION.toString() + ")");
            sender.sendMessage("§3Hilfe? /" + label + " help | Kommandos? /help minotopiacore");
            return true;
        } else {
            switch (args[0].toLowerCase()) {
                case "rename":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.rename", label + " rename")
                            || CommandHelper.kickConsoleFromMethod(sender, label + " rename")) {
                        return true;
                    }
                    Player renamePlayer = (Player) sender;
                    ItemStack stack = renamePlayer.getItemInHand();
                    if (stack == null || stack.getAmount() == 0) {
                        renamePlayer.sendMessage("§8Du hast nichts in der Hand!");
                        return true;
                    }
                    final String text = StringHelper.varArgsString(args, 1, true);
                    if (text.length() >= 60) {
                        renamePlayer.sendMessage("§8So lange Namen crashen Server :/ true story, bro!");
                        return true;
                    }
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(text);
                    stack.setItemMeta(meta);
                    renamePlayer.setItemInHand(stack);
                    renamePlayer.sendMessage("§7Der Name deines Items wurde auf §3" + text + "§7 gesetzt.");
                    return true;
                case "reload":
//                    if (!CommandHelper.checkActionPermAndMsg(sender, "mtc.cmd.mtc.reload", "MTC reloaden")) {
//                        return true;
//                    }
//                    CommandHelper.sendImportantActionMessage(sender, "Reloading MTC..");
//                    Bukkit.getPluginManager().disablePlugin(plugin);
//                    Bukkit.getPluginManager().enablePlugin(plugin);
//                    CommandHelper.sendImportantActionMessage(sender, "Reloaded MTC!");
                    sender.sendMessage("§cNo longer supported due to general issues! (configs, etc)");
                    return true;
                case "fm":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.fm", label + " fm")) {
                        return true;
                    }

                    String message = StringHelper.varArgsString(args, 1, true).replace("\\n", "\n");
                    BaseComponent[] adminComponents = TextComponent.fromLegacyText(message);
                    HoverEvent adminHover = new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new XyComponentBuilder("/xyu fm - " + senderName).create()
                    );
                    for (BaseComponent adminComponent : adminComponents) {
                        adminComponent.setHoverEvent(adminHover);
                    }

                    for (Player plr : Bukkit.getOnlinePlayers()) {
                        if (plr.hasPermission("mtc.spy")) {
                            plr.spigot().sendMessage(adminComponents);
                        } else {
                            CommandHelper.msg(message, plr);
                        }
                    }

                    return true;
                case "milk":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.milk", label + " milk")
                            || CommandHelper.kickConsoleFromMethod(sender, label + "milk")) {
                        return true;
                    }
                    Player milkTarget = (Player) sender;
                    for (PotionEffect pot : milkTarget.getActivePotionEffects()) {
                        milkTarget.removePotionEffect(pot.getType());
                    }
                    milkTarget.sendMessage("§7Du hast die Macht der §f§lMILCH §7benutzt.");
                    return true;
                case "ci":
                case "marceldavis":
                    if (CommandHelper.kickConsoleFromMethod(sender, label)) {
                        return true;
                    }
                    InventoryHelper.clearInventory((Player) sender);
                    sender.sendMessage("§7Marcel Davis von 1&1 hat deine Items gegessen.");
                    return true;
                case "sign":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.sign", label + " sign")
                            || CommandHelper.kickConsoleFromMethod(sender, label)) {
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage("§3/" + label + " sign <line#> <text>§7 <-- Benutzung");
                        return true;
                    }
                    int line;
                    try {
                        line = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§8Das ist keine Zahl.");
                        return true;
                    }
                    Player signPlayer = (Player) sender;
                    List<Block> targets = signPlayer.getLastTwoTargetBlocks((Set<Material>) null, 50);
                    Block target = targets.get(1);
                    if (target == null || !(target.getType() == Material.WALL_SIGN) && !(target.getType() == Material.SIGN_POST)) {
                        sender.sendMessage("§8Das nennst du ein Schild?! (" + target + ")");
                        return true;
                    }

                    Sign sgn = (Sign) target.getState();
                    sgn.setLine(line, StringHelper.varArgsString(args, 2, true));
                    sgn.update();
                    sender.sendMessage("§7Das Schild wurde editiert.");
                    return true;
                case "rne":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.rnentity", label + " rnentity")
                            || CommandHelper.kickConsoleFromMethod(sender, label)) {
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage("§3/" + label + " rne <neuer Name>§7 <-- Benutzung");
                        return true;
                    }
                    Player rnePlayer = (Player) sender;
                    List<Entity> ents = rnePlayer.getNearbyEntities(1, 1, 1);
                    if (ents.size() < 1) {
                        rnePlayer.sendMessage("§8FOREVER_ALONE.png");
                        return true;
                    }
                    Entity ent = ents.get(0);
                    if (!(ent instanceof LivingEntity)) {
                        rnePlayer.sendMessage("§8Das ist tot.");
                        return true;
                    }
                    ent.setCustomName(StringHelper.varArgsString(args, 1, true));
                    ent.setCustomNameVisible(true);
                    rnePlayer.sendMessage("§7Der Name des/der " + ent.getType().toString() + " in deiner Nähe wurde geändert.");
                    return true;
                case "spy":
                case "chatspy":
                case "nsa":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.spy", label)
                            || CommandHelper.kickConsoleFromMethod(sender, label)) {
                        return true;
                    }
                    if (MTCChatHelper.spies.contains(sender.getName())) {
                        MTCChatHelper.spies.remove(sender.getName());
                        sender.sendMessage(MTC.chatPrefix + "NSA-Modus deaktiviert!");
                        return true;
                    }
                    MTCChatHelper.spies.add(sender.getName());
                    sender.sendMessage(MTC.chatPrefix + "NSA-Modus aktiviert!");
                    return true;
                case "rstlng":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.rstlng", label)) {
                        return true;
                    }
                    Map<String, YamlConfiguration> map = new HashMap<>(); //TODO Contents of collection map are updated, but never queried at line 192
                    for (String lang : plugin.getShippedLocales()) {
                        String dir = "plugins/" + plugin.getName() + "/lang/";
                        String fl = lang + ".lng.yml";
                        File destFl = new File(dir + fl);
                        File destDir = new File(dir);
                        if (destFl.exists()) {
                            map.put(lang, YamlConfiguration.loadConfiguration(destFl));
                        } else {
                            try {
                                destDir.mkdirs(); //REFACTOR Result of method call ignored
                                destFl.createNewFile();
                                try (FileOutputStream out = new FileOutputStream(destFl);
                                     InputStream in = plugin.getResource("xyc_lang/" + lang + ".lng.yml")) {
                                    int read;
                                    while ((read = in.read()) != -1) {
                                        out.write(read);
                                    }
                                    out.flush();
                                    map.put(lang, YamlConfiguration.loadConfiguration(destFl));
                                }
                            } catch (IOException e) {
                                System.out.println("Could not copy XYC localization files from JAR: " + lang);
                                e.printStackTrace();
                            }
                        }
                    }
                    sender.sendMessage(MTC.chatPrefix + "Sprachdateien resettet!");
                    return true;
                case "clearcache":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.clearcache", label)) {
                        return true;
                    }
                    ClanHelper.clearCache();
                    plugin.getXLoginHook().resetSpawnLocation();
                    plugin.getServer().getPluginManager().callEvent(new ClearCacheEvent());
                    CacheHelper.clearCaches(true, plugin);
                    sender.sendMessage(MTC.chatPrefix + "Cache geleert.");
                    return true;
                case "forcecron":
                    if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.forcecron", label)) {
                        return true;
                    }
                    (new RunnableCronjob5Minutes(true, plugin)).run();
                    sender.sendMessage(MTC.chatPrefix + "Forced Cronjob (5m)!");
                    return true;
                case "config":
                    return this.handleConfigAction(sender, args, label);
                case "pid":
                    sender.sendMessage("§e" + ManagementFactory.getRuntimeMXBean().getName());
                    return true;
                default:
                    sender.sendMessage("§cUnbekannte Aktion.");
            }
            if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.help", label)) {
                return true;
            }

            if (args.length == 1) {
                HelpManager.tryPrintHelp("mtc", sender, label, "", "mtc help mtc");
            } else {
                String pageNum = ((args.length < 3) ? "" : args[2]);
                if (!HelpManager.tryPrintHelp(args[1].toLowerCase(), sender, args[1].toLowerCase(), pageNum, "mtc help " + args[1].toLowerCase())) {
                    sender.sendMessage("§8Kein Kommando mit diesem Namen vorhanden oder interner Fehler!");
                }
            }
        }

        return true;
    }

    private boolean handleConfigAction(CommandSender sender, String[] args, String label) {
        String action = args.length > 1 ? args[1] : "help";

        if (!CommandHelper.checkPermAndMsg(sender, "mtc.cmd.mtc.config." + args[1], label + " " + args[1])) {
            return true;
        }

        switch (action) {
            case "set": //mtc config set WHAT VAL
                if (args.length < 4) {
                    sender.sendMessage("§8Invalide Argumente für /" + label + " config set. Hilfe:");
                    HelpManager.tryPrintHelp("mtc", sender, label, "", "mtc help mtc");
                    return true;
                }
                final String strValue = args[3];
                Object value = strValue;

                if (StringUtils.isNumeric(strValue)) {
                    value = Integer.parseInt(strValue);
                } else {
                    if (strValue.equalsIgnoreCase("true") || strValue.equalsIgnoreCase("false")) {
                        value = Boolean.parseBoolean(strValue);
                    }
                }

                plugin.getConfig().set(args[2], value);
                plugin.saveConfig();
                sender.sendMessage("§7Konfigurationswert §3" + args[2] + "§7 gesetzt auf: §3" + value + ".");
                CommandHelper.sendImportantActionMessage(sender, "Set Config Value §3" + args[2] + "§a§o to §3" + value);
                break;
            case "get": //mtc config get WHAT
                if (args.length < 3) {
                    sender.sendMessage("§7Invalide Argumente für §3/" + label + " config get§7. Hilfe:");
                    HelpManager.tryPrintHelp("mtc", sender, label, "", "mtc help mtc");
                    return true;
                }
                final String fetchedValue = String.valueOf(plugin.getConfig().get(args[2]));
                sender.sendMessage("§7Der Wert §3" + args[2] + "§7 ist im Moment gesetzt auf: §3" + fetchedValue + "§e.");
                break;
            case "reload":
                plugin.reloadConfig();
                CommandHelper.sendImportantActionMessage(sender, "Reloaded MTC config.");
                break;
            default:
                sender.sendMessage("§8Unbekannte Aktion config " + args[1] + ". Hilfe:");
                HelpManager.tryPrintHelp("mtc", sender, label, "", "mtc help mtc");
        }
        return true;
    }
}
