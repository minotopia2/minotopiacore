/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.shop.ui.inventory.button;

import li.l1t.common.util.inventory.ItemStackFactory;
import li.l1t.mtc.module.shop.ui.inventory.ShopMenu;
import li.l1t.mtc.module.shop.ui.inventory.ShopSellMenu;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * A button that forwards the player to a new {@link ShopSellMenu}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-04-18
 */
public class OpenSellMenuButton implements MenuButton<ShopMenu> {
    public static final OpenSellMenuButton INSTANCE = new OpenSellMenuButton();

    @Override
    public ItemStack getItemStack(ShopMenu menu) {
        return new ItemStackFactory(Material.EMERALD)
                .displayName("§eItems verkaufen...")
                .lore("§7Klicke hier, um Items zu verkaufen.")
                .produce();
    }

    @Override
    public void handleMenuClick(InventoryClickEvent evt, ShopMenu menu) {
        ShopSellMenu.openMenu(menu.getPlayer(), menu.getModule());
    }
}