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

package li.l1t.mtc.module.shop.ui.inventory;

import com.google.common.base.Preconditions;
import li.l1t.common.util.inventory.ItemStackFactory;
import li.l1t.mtc.module.shop.ShopModule;
import li.l1t.mtc.module.shop.ui.inventory.button.MenuButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * A simple menu icon framework for the Shop inventory. Abstract base class for inventory menus for
 * trading.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-04-17
 */
public abstract class ShopMenu implements InventoryHolder {
    public static final int ROW_SIZE = 9;
    public static final int INVENTORY_SIZE = 6 * ROW_SIZE;
    public static final int CANVAS_SIZE = INVENTORY_SIZE - ROW_SIZE;
    private static final ItemStackFactory TOP_ROW_PLACEHOLDER =
            new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 8))
                    .displayName("§eHilfe: §6/shop help");
    protected final ShopModule module;
    private Inventory inventory;
    private final Player player;
    protected MenuButton[] topRowButtons = new MenuButton[ROW_SIZE];

    protected ShopMenu(Player player, ShopModule module) {
        this.player = player;
        this.module = module;
    }

    public void open() {
        player.openInventory(getInventory());
    }

    @Override
    public Inventory getInventory() {
        //lazy init to circumvent title possibly being unknown at creation time
        if (inventory == null) {
            createNewInventory();
        }
        return inventory;
    }

    /**
     * Replaces this menu's inventory with a new one with the current title. Does not send this
     * change to the player.
     */
    protected void createNewInventory() {
        inventory = module.getPlugin().getServer()
                .createInventory(this, INVENTORY_SIZE, getInventoryTitle());
    }

    /**
     * @return the player holding this menu
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the top row button with given index for this menu, or null if there is no button at that
     * index.
     *
     * @param index the index to get the button for (0 &lt;= index &lt; 9)
     * @return the button, or null if there is none
     */
    public MenuButton getTopRowButton(int index) {
        Preconditions.checkArgument(index >= 0 && index < 9,
                "index must be positive and less than 10, given: %s", index);
        return topRowButtons[index];
    }

    /**
     * Sets the top row button with given index for this menu.
     *
     * @param index  the index to set the button for (0 &lt;= index &lt; 9)
     * @param button the button to set, or null to set no button
     */
    protected void setTopRowButton(int index, MenuButton button) {
        Preconditions.checkArgument(index >= 0 && index < 9,
                "index must be positive and less than 10, given: %s", index);
        topRowButtons[index] = button;
    }

    /**
     * @return the module managing this menu
     */
    public ShopModule getModule() {
        return module;
    }

    /**
     * Handles a click on this menu.
     *
     * @param evt the event causing the click
     * @return whether the event should be cancelled
     */
    public boolean handleClick(InventoryClickEvent evt) {
        if (!evt.getClick().isLeftClick()) {
            return true;
        }

        int slotId = evt.getSlot();
        if (slotId < ROW_SIZE) { //top row
            MenuButton button = topRowButtons[slotId];
            if (button != null) {
                button.handleMenuClick(evt, this);
            }
        } else {
            handleCanvasClick(evt, slotId - ROW_SIZE);
        }
        return true;
    }

    /**
     * Handles a click on this menu on the {@link org.bukkit.event.EventPriority#MONITOR} priority.
     * Note that this only gets called if {@link #handleClick(InventoryClickEvent)} hasn't cancelled
     * the event. This is useful for when you need the action to actually be performed for your
     * logic.
     *
     * @param evt the event causing the click
     */
    public void handleClickMonitor(InventoryClickEvent evt) {
        //no action by default
    }

    /**
     * Handles this inventory being closed.
     *
     * @param evt the event causing the close
     */
    public void handleClose(InventoryCloseEvent evt) {
        //no-op
    }

    /**
     * Figures out whether this menu permits hotbar swap for a given event. This also checks for
     * moving to the other inventory using shift.
     *
     * @param evt the event to process
     * @return whether hotbar swap is permitted
     */
    public boolean permitsHotbarSwap(InventoryClickEvent evt) {
        return false;
    }

    /**
     * Renders all buttons registered for the top menu.
     */
    @SuppressWarnings("unchecked")
    protected void renderTopMenu() {
        for (int i = 0; i < topRowButtons.length; i++) {
            MenuButton button = topRowButtons[i];
            if (button != null) {
                getInventory().setItem(i, button.getItemStack(this));
            } else {
                getInventory().setItem(i, TOP_ROW_PLACEHOLDER.produce()); //light gray - practically invisible
            }
        }
    }

    /**
     * Handles a click into the canvas area (e.g. everything except the top row) of this menu.
     *
     * @param canvasId the index of the clicked slot in the canvas (slotId - {@value #ROW_SIZE})
     * @param evt      the event causing the click
     */
    protected abstract void handleCanvasClick(InventoryClickEvent evt, int canvasId);

    protected abstract String getInventoryTitle();
}
