/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package io.github.xxyy.mtc.module.shop.ui.inventory;

import io.github.xxyy.common.util.CommandHelper;
import io.github.xxyy.common.util.inventory.ItemStackFactory;
import io.github.xxyy.mtc.hook.VaultHook;
import io.github.xxyy.mtc.module.shop.ShopItem;
import io.github.xxyy.mtc.module.shop.ShopModule;
import io.github.xxyy.mtc.module.shop.ShopPriceCalculator;
import io.github.xxyy.mtc.module.shop.TransactionType;
import io.github.xxyy.mtc.module.shop.ui.inventory.button.BackToListButton;
import io.github.xxyy.mtc.module.shop.ui.inventory.button.GenericButton;
import io.github.xxyy.mtc.module.shop.ui.util.ShopStringAdaptor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * This inventory allows players to view details about a shop item and buy or sell it.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-04-17
 */
public class ShopDetailMenu extends ShopMenu {
    private static final int[][] ITEM_AMOUNT_ROWS = new int[][]{
            new int[ROW_SIZE],
            new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9},
            new int[]{10, 15, 20, 25, 30, 35, 40, 45, 50},
            new int[]{12, 16, 24, 28, 32, 36, 42, 48, 64},
            new int[ROW_SIZE]
    };
    private final ShopItem item;
    private final ShopPriceCalculator priceCalculator = new ShopPriceCalculator(getModule().getItemManager());

    public ShopDetailMenu(Player player, ShopModule module, ShopItem item) {
        super(player, module);
        this.item = item;
        initTopRow();
        renderCanvas();
    }

    private void initTopRow() {
        setTopRowButton(0, BackToListButton.INSTANCE);
        setTopRowButton(3, new GenericButton(ShopInventoryHelper.createInfoStack(item), null));
        //TODO: 4 sell
        //TODO: 5 custom amount
        setTopRowButton(8, BackToListButton.INSTANCE);
    }

    private void renderCanvas() {
        if (item.canBeBought()) {
            for (int i = 0; i < ITEM_AMOUNT_ROWS.length; i++) {
                int[] amountRow = ITEM_AMOUNT_ROWS[i];
                for (int j = 0; j < amountRow.length; i++) {
                    int amount = amountRow[j];
                    if (amount == 0) {
                        continue;
                    }
                    int canvasId = i * ROW_SIZE + j;
                    int slotId = canvasId + ROW_SIZE;
                    double totalPrice = priceCalculator.calculatePrice(item, amount, TransactionType.BUY);

                    if (canAfford(totalPrice)) {
                        getInventory().setItem(slotId, createIconStack(amount, totalPrice));
                    } else {
                        getInventory().setItem(slotId, createNotAffordableStack(amount, totalPrice));
                    }
                }
            }
        } else {
            getInventory().setItem(2 * ROW_SIZE + 4, new ItemStackFactory(Material.BARRIER)
                    .lore("§cDieses Item kann nicht gekauft werden!")
                    .produce()
            );
        }
    }

    private boolean canAfford(double totalPrice) {
        VaultHook vaultHook = getModule().getPlugin().getVaultHook();
        return vaultHook == null || vaultHook.canAfford(getPlayer(), totalPrice);
    }

    @Override
    protected void handleCanvasClick(InventoryClickEvent evt, int canvasId) {
        int row = Math.floorDiv(canvasId, ROW_SIZE);
        int column = canvasId % ROW_SIZE;
        int amount = ITEM_AMOUNT_ROWS[row][column];
        if (amount != 0) {
            getModule().getTransactionExecutor()
                    .attemptTransaction(getPlayer(), item, amount, TransactionType.BUY);
            renderCanvas();
        }
    }

    @Override
    protected String getInventoryTitle() {
        return CommandHelper.sixteenCharColorize(item.getDisplayName(), "§6§l");
    }

    /**
     * @return the item detailed by this inventory
     */
    public ShopItem getItem() {
        return item;
    }

    /**
     * Opens a new shop detail menu for a given player.
     *
     * @param player the player to open the menu for
     * @param module the shop module managing the menu
     * @param item   the item to show details for
     * @return the created menu
     */
    public static ShopDetailMenu openMenu(Player player, ShopModule module, ShopItem item) {
        ShopDetailMenu menu = new ShopDetailMenu(player, module, item);
        menu.open();
        return menu;
    }

    private ItemStack createIconStack(int amount, double totalPrice) {
        return new ItemStackFactory(item.toItemStack(amount))
                .lore("§7Klicke hier, um §8" + amount + "§7 Stück")
                .lore("§8" + item.getDisplayName() + "§7 zu kaufen.")
                .lore(" ")
                .lore("§eKaufpreis: §7" + ShopStringAdaptor.getCurrencyString(totalPrice))
                .lore("§eStückpreis: §7" + ShopStringAdaptor.getCurrencyString(item.getBuyCost()))
                .produce();
    }

    private ItemStack createNotAffordableStack(int amount, double totalPrice) {
        return new ItemStackFactory(Material.BARRIER)
                .lore("§cDu kannst dir §4" + amount)
                .lore("§4" + item.getDisplayName() + "§c nicht leisten!")
                .lore(" ")
                .lore("§eKaufpreis: §7" + ShopStringAdaptor.getCurrencyString(totalPrice))
                .lore("§eStückpreis: §7" + ShopStringAdaptor.getCurrencyString(item.getBuyCost()))
                .produce();
    }


}
