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

package li.l1t.mtc.module.shop.ui.inventory.button;

import com.google.common.base.Preconditions;
import li.l1t.common.util.inventory.ItemStackFactory;
import li.l1t.mtc.hook.VaultHook;
import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.shop.ShopPriceCalculator;
import li.l1t.mtc.module.shop.TransactionType;
import li.l1t.mtc.module.shop.api.ShopItem;
import li.l1t.mtc.module.shop.ui.inventory.ShopMenu;
import li.l1t.mtc.module.shop.ui.inventory.ShopSellMenu;
import li.l1t.mtc.module.shop.ui.util.ShopStringAdaptor;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Button which sells all the items currently present in a {@link ShopSellMenu}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-04-18
 */
public class SellButton implements MenuButton<ShopSellMenu> {
    private static final Logger LOGGER = LogManager.getLogger(SellButton.class);
    private final ShopPriceCalculator priceCalculator;

    public SellButton(ShopPriceCalculator priceCalculator) {
        this.priceCalculator = priceCalculator;
    }

    @Override
    public ItemStack getItemStack(ShopSellMenu menu) {
        return new ItemStackFactory(Material.EMERALD)
                .displayName("§3§lJetzt verkaufen!")
                .lore("§6Gesamtwert:")
                .lore("§e" + calculateMenuWorth(menu))
                .produce();
    }

    @Override
    public void handleMenuClick(InventoryClickEvent evt, ShopSellMenu menu) {
        double totalWorth = calculateMenuWorth(menu);
        VaultHook vaultHook = menu.getModule().getPlugin().getVaultHook();
        if (vaultHook != null) {
            vaultHook.depositPlayer(menu.getPlayer(), totalWorth);
            clearSoldItems(menu);
            menu.clearCanvas();
            menu.getModule().getTextOutput()
                    .sendPrefixed(menu.getPlayer(), "Du hast §e" +
                            ShopStringAdaptor.getCurrencyString(totalWorth) +
                            "§6 eingenommen.");
        } else {
            menu.getPlayer().sendMessage("§cKonnte nicht mit dem Geldplugin reden :(");
        }
    }

    private void clearSoldItems(ShopMenu menu) {
        ItemStack[] contents = menu.getInventory().getContents();
        for (int slotId = ShopMenu.ROW_SIZE; slotId < contents.length; slotId++) {
            ItemStack stack = contents[slotId];
            menu.getModule().getItemManager().getItem(stack)
                    .filter(ShopItem::canBeSold)
                    .ifPresent(clearSoldItem(menu, slotId, stack));
        }
    }

    private Consumer<ShopItem> clearSoldItem(ShopMenu menu, int slotId, ItemStack stack) {
        return item -> {
            menu.getInventory().setItem(slotId, null);
            LOGGER.info("Inventory Transaction {}: SELL {} x{} ({})",
                    menu.getPlayer().getName(), item.getSerializationName(),
                    stack.getAmount(), menu.getPlayer().getUniqueId());
        };
    }

    private double calculateMenuWorth(ShopMenu menu) {
        Preconditions.checkArgument(menu instanceof ShopSellMenu,
                "menu must be ShopSellMenu, got %s", menu.getClass());
        ItemStack[] contents = menu.getInventory().getContents();
        //we don't want to sell the top menu, lel
        ItemStack[] canvasContents = Arrays.copyOfRange(contents, ShopMenu.ROW_SIZE, contents.length);

        return priceCalculator.sumPrices(
                Arrays.asList(canvasContents),
                TransactionType.SELL
        );
    }
}
