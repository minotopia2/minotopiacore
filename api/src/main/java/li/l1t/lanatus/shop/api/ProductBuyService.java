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

package li.l1t.lanatus.shop.api;

import li.l1t.lanatus.api.product.Product;
import li.l1t.lanatus.shop.api.metrics.PurchaseRecorder;
import org.bukkit.entity.Player;

/**
 * A service that helps with buying products.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-16-11
 */
public interface ProductBuyService {
    /**
     * Attempts to purchase given product with given player's account, notifying them of the status of the purchase once
     * complete.
     *
     * @param player  the player whose account to use
     * @param product the product to purchase
     */
    void attemptPurchase(Player player, Product product);

    /**
     * @param player th player whose melons count to retrieve
     */
    int findCurrentMelonsCount(Player player);

    /**
     * Sets the pruchase recorder this service notifer of purchases.
     *
     * @param recorder the recoorder to notify
     */
    void setPurchaseRecorder(PurchaseRecorder recorder);
}
