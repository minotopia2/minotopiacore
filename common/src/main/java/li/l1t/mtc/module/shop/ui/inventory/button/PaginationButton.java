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
import li.l1t.mtc.module.shop.ui.inventory.ShopListMenu;
import li.l1t.mtc.module.shop.ui.inventory.ShopMenu;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Represents pagination buttons visualised through player heads to be used with {@link
 * ShopListMenu}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-04-17
 */
public class PaginationButton implements MenuButton<ShopListMenu> {
    public static final PaginationButton FIRST_PAGE = new PaginationButton(PaginationAction.FIRST);
    public static final PaginationButton PREVIOUS_PAGE = new PaginationButton(PaginationAction.PREVIOUS);
    public static final PaginationButton NEXT_PAGE = new PaginationButton(PaginationAction.NEXT);
    public static final PaginationButton LAST_PAGE = new PaginationButton(PaginationAction.LAST);

    private final PaginationAction action;
    private final ItemStack itemStack;

    PaginationButton(PaginationAction action) {
        Preconditions.checkNotNull(action, "action");
        this.action = action;
        this.itemStack = new ItemStackFactory(action.getDisplayMaterial())
                .displayName("§b" + action.getDisplayName())
                .produce();
    }

    @Override
    public ItemStack getItemStack(ShopListMenu menu) {
        return itemStack;
    }

    @Override
    public void handleMenuClick(InventoryClickEvent evt, ShopListMenu menu) {
        menu.setItemStart(action.getTargetItemStart(menu));
        menu.renderCurrentPage();
    }

    private enum PaginationAction {
        FIRST(Material.POWERED_MINECART, "<< Zur ersten Seite") {
            @Override
            int getTargetItemStart(ShopListMenu menu) {
                return firstPage();
            }
        },
        PREVIOUS(Material.MINECART, "< Zur vorherigen Seite") {
            @Override
            int getTargetItemStart(ShopListMenu menu) {
                if (menu.getCurrentItemStart() < ShopMenu.CANVAS_SIZE) {
                    return lastPage(menu);
                } else {
                    return menu.getCurrentItemStart() - ShopMenu.CANVAS_SIZE;
                }
            }
        },
        NEXT(Material.MINECART, "Zur nächsten Seite >") {
            @Override
            int getTargetItemStart(ShopListMenu menu) {
                if (menu.getCurrentItemStart() >= lastPage(menu)) {
                    return firstPage();
                } else {
                    return menu.getCurrentItemStart() + ShopMenu.CANVAS_SIZE;
                }
            }
        },
        LAST(Material.POWERED_MINECART, "Zur letzten Seite >>") {
            @Override
            int getTargetItemStart(ShopListMenu menu) {
                return lastPage(menu);
            }
        };

        private final Material displayMaterial;
        private final String displayName;

        PaginationAction(Material displayMaterial, String displayName) {
            this.displayMaterial = displayMaterial;
            this.displayName = displayName;
        }

        /**
         * Gets the index of the first item to be displayed after this button is clicked.
         *
         * @param menu the menu to get the index for
         * @return the index
         */
        abstract int getTargetItemStart(ShopListMenu menu);

        int lastPage(ShopListMenu menu) {
            if (menu.getItems().size() <= ShopMenu.CANVAS_SIZE) {
                return firstPage();
            }
            int itemsOnLastPage = menu.getItems().size() % ShopMenu.CANVAS_SIZE;
            int freeSlotsOnLastPage = ShopMenu.CANVAS_SIZE - itemsOnLastPage;
            return menu.getItems().size() - ShopMenu.CANVAS_SIZE + freeSlotsOnLastPage;
        }

        private static int firstPage() {
            return 0;
        }

        public Material getDisplayMaterial() {
            return displayMaterial;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
