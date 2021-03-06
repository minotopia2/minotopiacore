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

package li.l1t.mtc.module.chal;

import li.l1t.common.misc.XyLocation;
import li.l1t.common.util.inventory.InventoryHelper;
import li.l1t.common.util.inventory.ItemStackFactory;
import li.l1t.mtc.helper.MTCHelper;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stores the location of a chest for Chal, including metadata.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30/11/14
 */
public class ChestLocation extends XyLocation {
    private ChalDate date;

    public ChestLocation(Location toClone, ChalDate date) {
        super(toClone);
        this.date = date;
    }

    public ChalDate getDate() {
        return date;
    }

    public void setDate(ChalDate date) {
        this.date = date;
    }

    public boolean openableBy(Player plr) {
        return plr.hasPermission(ChalModule.DATE_BYPASS_PERMISSION) || date.is(LocalDate.now()) ||
                (date.before(LocalDate.now()) && plr.hasPermission(ChalModule.DATE_BYPASS_PAST_PERMISSION));
    }

    public boolean openFor(Player plr) {
        if (!openableBy(plr)) {
            return !MTCHelper.sendLocArgs("XU-chopendenied", plr, false, date.toReadable());
        }

        if (getBlock() == null || getBlock().getType() != Material.CHEST) {
            plr.sendMessage(String.format("§cInterner Fehler. Melde dies bitte. (Tür #%d)", date.getDay()));
            return false;
        }

        Chest chest = (Chest) getBlock().getState();
        ItemStack[] contents = InventoryHelper.cloneAll(chest.getBlockInventory().getContents());
        List<String> lore = Arrays.asList("§eAdventskalender Tür #" + date.getDay(), "§6geöffnet von " + plr.getName());
        contents = Arrays.asList(contents).stream()
                .filter(Objects::nonNull)
                .map(ItemStackFactory::new)
                .map(isf -> isf.appendLore(lore))
                .map(ItemStackFactory::produce)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()).toArray(new ItemStack[1]);
        plr.getInventory().addItem(contents);
        MTCHelper.sendLocArgs("XU-chopened", plr, false, date.getDay());

        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("date", date.serialize());
        return result;
    }

    public static ChestLocation deserialize(Map<String, Object> input) {
        Validate.isTrue(input.containsKey("date"), "Missing date!");
        ChalDate date = ChalDate.deserialize(input.get("date").toString());
        return new ChestLocation(XyLocation.deserialize(input), date);
    }
}
