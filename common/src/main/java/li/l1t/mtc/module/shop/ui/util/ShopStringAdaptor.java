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

package li.l1t.mtc.module.shop.ui.util;

import li.l1t.mtc.module.shop.TransactionType;
import li.l1t.mtc.module.shop.api.ShopItem;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Adapts strings to different arguments, for proper grammar. This class aims to prevent things like
 * "1 items" from happening since these look unprofessional. Static utility class.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2015-10-31
 */
public class ShopStringAdaptor {
    public static final String CURRENCY_SINGULAR = "MineCoin";
    public static final String CURRENCY_PLURAL = "MineCoins";
    private static final NumberFormat CURRENCY_FORMAT;

    static {
        CURRENCY_FORMAT = NumberFormat.getNumberInstance(Locale.GERMAN);
        CURRENCY_FORMAT.setGroupingUsed(true);
        CURRENCY_FORMAT.setMaximumFractionDigits(2);
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
        CURRENCY_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    private ShopStringAdaptor() {

    }

    /**
     * Returns a human-readable representation of given currency value. This respects singular and
     * plural forms.
     *
     * @param amount the amount to format
     * @return a human-readable representation of given value, including currency name
     */
    public static String getCurrencyString(double amount) {
        return amount == 1 ?
                "einen " + CURRENCY_SINGULAR :
                CURRENCY_FORMAT.format(amount) + " " + CURRENCY_PLURAL;
    }

    /**
     * Just formats a currency value for display according to the internal currency format. Does not
     * append currency name.
     *
     * @param amount the amount to format
     * @return the formatted value
     * @see #getCurrencyString(double)
     */
    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Gets the human-readable display string of given item with given amount.
     *
     * @param item   the item to format
     * @param amount the amount to format
     * @return the human-readable display string
     */
    public static String getAdjustedDisplayName(ShopItem item, int amount) {
        return (amount == 1 ? "ein" : amount) + " " + item.getDisplayName();
    }

    /**
     * Returns a human-readable verb representation of given transaction type as a Participle II.
     *
     * @param type the type to get the Participle II for
     * @return a human-readable Participle II representation of given type
     */
    public static String getParticipleII(TransactionType type) {
        if (type == null) {
            return "gehandelt";
        }

        switch (type) {
            case SELL:
                return "verkauft";
            case BUY:
                return "gekauft";
            default:
                throw new AssertionError("Unknown transaction type " + type);
        }
    }

    /**
     * Returns a human-readable verb representation of given transaction type as Infinitive.
     *
     * @param type the type to get the Infinitive for
     * @return a human-readable Infinitive representation of given type
     */
    public static String getInfinitive(TransactionType type) {
        if (type == null) {
            return "handeln";
        }

        switch (type) {
            case SELL:
                return "verkaufen";
            case BUY:
                return "kaufen";
            default:
                throw new AssertionError("Unknown transaction type " + type);
        }
    }
}
