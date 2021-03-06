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

package li.l1t.mtc.module.lanatus.shop.gui;

import li.l1t.common.inventory.SlotPosition;
import li.l1t.common.inventory.gui.ChildMenu;
import li.l1t.common.inventory.gui.TopRowMenu;
import li.l1t.common.inventory.gui.element.LambdaMenuElement;
import li.l1t.common.inventory.gui.element.Placeholder;
import li.l1t.common.inventory.gui.element.button.BackToParentButton;
import li.l1t.common.util.inventory.ItemStackFactory;
import li.l1t.lanatus.api.product.Product;
import li.l1t.lanatus.shop.api.ItemIconService;
import li.l1t.lanatus.shop.api.ProductBuyService;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


/**
 * Displays a menu for displaying a product and offers an option to purchase it.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-18-11
 */
public class ProductDetailMenu extends TopRowMenu implements ChildMenu {
    private final Product product;
    private final ProductSelectionMenu parent;
    private final ItemIconService iconService;
    private final ProductBuyService buyService;

    public ProductDetailMenu(Product product, ProductSelectionMenu parent, ItemIconService iconService, ProductBuyService buyService) {
        super(parent.getPlugin(), "§e§l" + product.getDisplayName(), parent.getPlayer());
        this.product = product;
        this.parent = parent;
        this.iconService = iconService;
        this.buyService = buyService;
        initTopRow();
    }

    @Override
    protected void initTopRow() {
        addToTopRow(0, BackToParentButton.INSTANCE);
        addToTopRow(1, new Placeholder(iconService.createIconStack(parent.getCategory())));
        addToTopRow(2, new Placeholder(iconService.createIconStack(product, getPlayer().getUniqueId())));
        addToTopRow(3, new Placeholder(iconService.createPurchaseHelpStack()));
        addToTopRow(8, BackToParentButton.INSTANCE);
        addElement(SlotPosition.ofXY(1, 3), confirmButton());
        addElement(SlotPosition.ofXY(7, 3), abortButton());
    }

    @SuppressWarnings("deprecation")
    private LambdaMenuElement<ProductDetailMenu> confirmButton() {
        return new LambdaMenuElement<>(ProductDetailMenu.class, this::handleConfirm, confirmStack());
    }

    @NotNull
    private ItemStack confirmStack() {
        return new ItemStackFactory(new ItemStack(Material.STAINED_CLAY, 1, woolData(DyeColor.GREEN)))
                .displayName("§a§lBestätigen")
                .lore("§7Hier klicken, um dieses").lore("§7Produkt zu kaufen")
                .lore(" ").lore("§e" + product.getMelonsCost() + " " + melonPlural(product.getMelonsCost()))
                .produce();
    }

    @SuppressWarnings("deprecation")
    private byte woolData(DyeColor color) {
        return color.getWoolData();
    }

    private String melonPlural(int melonsCount) {
        return "Melone" + (melonsCount == 1 ? "" : "n");
    }

    private LambdaMenuElement<ProductDetailMenu> abortButton() {
        return new LambdaMenuElement<>(ProductDetailMenu.class, this::handleAbort, abortStack());
    }

    @NotNull
    private ItemStack abortStack() {
        return new ItemStackFactory(new ItemStack(Material.STAINED_CLAY, 1, woolData(DyeColor.RED)))
                .displayName("§c§lAbbrechen")
                .lore("§7Hier klicken, um").lore("§7abzubrechen")
                .produce();
    }

    private void handleAbort(InventoryClickEvent evt, ProductDetailMenu menu) {
        getParent().open();
    }

    private void handleConfirm(InventoryClickEvent evt, ProductDetailMenu menu) {
        buyService.attemptPurchase(getPlayer(), product);
    }

    @Override
    public ProductSelectionMenu getParent() {
        return parent;
    }
}
