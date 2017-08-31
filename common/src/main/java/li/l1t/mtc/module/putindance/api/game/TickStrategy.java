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

package li.l1t.mtc.module.putindance.api.game;

import li.l1t.common.misc.XyLocation;

/**
 * Defines the behaviour of a game when it is ticked.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-21
 */
public interface TickStrategy {
    void tick(Game game);

    boolean isReady();

    /**
     * Checks whether this strategy can tick a board with given boundaries. Order of arguments does
     * not matter.
     *
     * @param firstBoundary  the first boundary
     * @param secondBoundary the second boundary
     * @throws InvalidBoardSelectionException if this strategy cannot handle a board with given
     *                                        boundaries
     */
    void checkBoard(XyLocation firstBoundary, XyLocation secondBoundary) throws InvalidBoardSelectionException;
}
