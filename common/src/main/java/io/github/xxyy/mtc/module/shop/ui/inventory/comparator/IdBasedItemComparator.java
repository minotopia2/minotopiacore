/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package io.github.xxyy.mtc.module.shop.ui.inventory.comparator;

import io.github.xxyy.mtc.module.shop.ShopItem;

import java.util.Comparator;

/**
 * Sorts shop items based on the underlying material id and data value.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-07-10
 */
public class IdBasedItemComparator implements Comparator<ShopItem> {
    @Override
    public int compare(ShopItem o1, ShopItem o2) {
        int result;
        result = Integer.compare(o1.getMaterial().ordinal(), o2.getMaterial().ordinal());
        if (result == 0) {
            result = Short.compare(o1.getDataValue(), o2.getDataValue());
        }
        return result;
    }
}
