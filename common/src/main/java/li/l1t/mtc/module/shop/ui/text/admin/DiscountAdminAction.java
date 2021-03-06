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

package li.l1t.mtc.module.shop.ui.text.admin;

import li.l1t.common.chat.ComponentSender;
import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.common.util.CommandHelper;
import li.l1t.common.util.StringHelper;
import li.l1t.mtc.module.shop.ShopModule;
import li.l1t.mtc.module.shop.api.ShopItem;
import li.l1t.mtc.module.shop.ui.text.AbstractShopAction;
import li.l1t.mtc.module.shop.ui.util.ShopStringAdaptor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Admin action that displays or sets the discounted price for shop items.
 *
 * @author Janmm14, xxyy
 */
class DiscountAdminAction extends AbstractShopAction {
    private final ShopModule module;

    DiscountAdminAction(ShopModule module) {
        super("shopadmin", "discount", 1, null);
        this.module = module;
    }

    @Override
    public void execute(String[] args, Player plr, String label) {
        int ignoreArgsEnd = args.length == 1 ? 0 : 1; //only strip args if we have anything left then
        String itemName = StringHelper.varArgsString(args, 0, ignoreArgsEnd, false);
        ShopItem item = module.getItemManager().getItem(plr, itemName).orElse(null);
        if (module.getTextOutput().checkNonExistant(plr, item, itemName)) {
            return;
        }

        if (args.length == 1) {
            sendDiscountInfo(item, plr);
            return;
        }

        try {
            double newDiscountPrice = Double.parseDouble(args[args.length - 1]);
            handleSet(item, plr, newDiscountPrice);
            sendDiscountInfo(item, plr);
        } catch (NumberFormatException nfe) {
            if (args.length > 1) {
                plr.sendMessage("§cReduzierter Preis muss eine Zahl sein!");
            }
        }
    }

    private void sendDiscountInfo(ShopItem item, CommandSender sender) {
        XyComponentBuilder builder = new XyComponentBuilder("Das Item ", ChatColor.GOLD)
                .append(item.getDisplayName(), ChatColor.YELLOW).event(module.getTextOutput().createItemHover(item))
                .append(" ", ChatColor.GOLD, ComponentBuilder.FormatRetention.FORMATTING);

        if (item.isDiscountable()) {
            builder.append("kostet im reduzierten Status ")
                    .append(ShopStringAdaptor.getCurrencyString(item.getDiscountedPrice()), ChatColor.YELLOW)
                    .append(" (-" + item.getDiscountPercentage() + "%)");
        } else {
            builder.append("kann nicht reduziert werden");
        }

        ComponentSender.sendTo(builder.append(".", ChatColor.GOLD).create(), sender);
    }

    private boolean handleSet(ShopItem item, CommandSender sender, double discountedPrice) {
        if (discountedPrice < item.getSellWorth() && discountedPrice != ShopItem.NOT_DISCOUNTABLE) {
            return !CommandHelper.msg(String.format(
                    "§cDer reduzierte Preis §e%s §ckann nicht geringer als der Verkaufswert §e%s §csein!",
                    discountedPrice, item.getSellWorth()
            ), sender);
        }
        if (discountedPrice >= item.getBuyCost() && discountedPrice != ShopItem.NOT_DISCOUNTABLE) {
            return !CommandHelper.msg(String.format(
                    "§cDer reduzierte Preis §e%s §cmuss geringer als der Kaufpreis §e%s §csein!",
                    discountedPrice, item.getBuyCost()
            ), sender);
        }

        item.setDiscountedPrice(discountedPrice);
        module.getItemConfig().updateItem(item);
        sender.sendMessage("§aNeuer reduzierter Preis gesetzt:");
        return true;
    }

    @Override
    public void sendHelpLines(Player plr) {
        sendHelpLine(plr, "<Item> <reduzierter Preis>", "Setzt den reduzierten Preis für ein Item (0 = aus)");
        sendHelpLine(plr, "<Item>", "Zeigt den reduzierten Preis für ein Item");
    }
}
