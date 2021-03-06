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

package li.l1t.mtc.module.shop.transaction;

import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.shop.ShopModule;
import li.l1t.mtc.module.shop.TransactionType;
import li.l1t.mtc.module.shop.api.ShopItem;
import li.l1t.mtc.module.shop.api.TransactionInfo;
import li.l1t.mtc.module.shop.ui.text.ShopTextOutput;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Controls shop transactions, managing both sides in terms of economy and items.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/10/15
 */
public class ShopTransactionExecutor {
    private static final Logger LOGGER = LogManager.getLogger(ShopTransactionExecutor.class);
    private final ShopTextOutput output;
    private final TransactionHandler economyHandler;
    private final TransactionHandler inventoryHandler;

    public ShopTransactionExecutor(ShopModule module) {
        this.output = module.getTextOutput();
        this.economyHandler = new ShopEconomyHandler(module.getItemManager(), module.getPlugin().getVaultHook());
        this.inventoryHandler = new ShopInventoryHandler();
    }

    /**
     * Attempts to execute a complete transaction. If any part of the transaction fails, anything
     * already done will be reversed and a nice error message will be sent to the player.
     *
     * @param plr    the player initiating the transaction
     * @param item   the item involved in the transaction
     * @param amount the amount of the item requested to be transferred
     * @param type   the type of the transaction
     * @return whether the transaction was successful
     */
    public boolean attemptTransaction(Player plr, ShopItem item, int amount, TransactionType type) {
        if (attemptTransactionInternal(plr, item, amount, type)) {
            output.sendTransactionSuccess(plr, item, amount, type);
            return true;
        }

        return false;
    }

    /**
     * Attempts to execute a complete transaction. If any part of the transaction fails, anything
     * already done will be reversed. No messages will be printed except in case of failure.
     *
     * @param plr    the player initiating the transaction
     * @param item   the item involved in the transaction
     * @param amount the amount of the item requested to be transferred
     * @param type   the type of the transaction
     * @return whether the transaction was successful
     */
    public boolean attemptTransactionSilent(Player plr, ShopItem item, int amount, TransactionType type) {
        return attemptTransactionInternal(plr, item, amount, type);
    }

    private boolean attemptTransactionInternal(Player plr, ShopItem item, int amount, TransactionType type) {
        /*
        For simplicity reasons, the action that takes the player's payment is always called first. So if they cannot
        deliver, the action will be canceled right away. Us not being able to deliver is far less probable.
         */

        TransactionHandler[] handlers;
        switch (type) {
            case BUY:
                handlers = new TransactionHandler[]{economyHandler, inventoryHandler};
                break;
            case SELL:
                handlers = new TransactionHandler[]{inventoryHandler, economyHandler};
                break;
            default:
                throw new AssertionError("invalid type: " + type);
        }

        return callHandlers(plr, item, amount, type, handlers);
    }

    private boolean callHandlers(Player plr, ShopItem item, int amount, TransactionType type,
                                 TransactionHandler... handlers) {
        Set<TransactionInfo> succeededSteps = new HashSet<>(handlers.length);
        for (TransactionHandler handler : handlers) {
            TransactionInfo info = handler.execute(plr, item, amount, type);
            if (!info.isSuccessful()) {
                output.sendTransactionFailure(plr, type, info.getTransactionError());
                succeededSteps.forEach(TransactionInfo::revoke);
                LOGGER.info(
                        "Failed Transaction {}: {} {} x{} ({}) [{}]",
                        plr.getName(), type.name(), item.getSerializationName(),
                        amount, plr.getUniqueId(), info.getTransactionError()
                );
                return false;
            } else {
                succeededSteps.add(info);
            }
        }
        LOGGER.info(
                "Successful Transaction {}: {} {} x{} ({})",
                plr.getName(), type.name(), item.getSerializationName(), amount, plr.getUniqueId()
        );
        return true;
    }
}
