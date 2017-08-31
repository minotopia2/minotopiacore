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

package li.l1t.mtc.misc;

/**
 * Lists standard behaviours of configuration files upon cache clear
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 19/01/15
 */
public enum ClearCacheBehaviour {
    /**
     * Reloads the config file upon any cache clear.
     */
    RELOAD,
    /**
     * Saves the config file upon any cache clear.
     */
    SAVE,
    /**
     * Saves the config file for normal cache clears and reloads it for forced ones.
     */
    RELOAD_ON_FORCED,
    /**
     * Does nothing on cache clear.
     */
    NOTHING
}
