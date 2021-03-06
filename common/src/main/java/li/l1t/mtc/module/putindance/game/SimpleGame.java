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

package li.l1t.mtc.module.putindance.game;

import com.google.common.base.Preconditions;
import li.l1t.common.misc.XyLocation;
import li.l1t.mtc.module.putindance.api.board.Board;
import li.l1t.mtc.module.putindance.api.game.Game;
import li.l1t.mtc.module.putindance.api.game.TickStrategy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Stores metadata related to a game of PutinDance.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-20
 */
public class SimpleGame implements Game {
    private final GameAnnouncer gameAnnouncer = new GameAnnouncer();
    private final Board board;
    private final TickStrategy tickStrategy;
    private XyLocation spawnLocation;
    private boolean open;

    public SimpleGame(Board board, TickStrategy tickStrategy) {
        this.board = Preconditions.checkNotNull(board, "board");
        this.tickStrategy = Preconditions.checkNotNull(tickStrategy, "tickStrategy");
        tickStrategy.checkBoard(board.getFirstBoundary(), board.getSecondBoundary());
    }

    @Override
    public void addPlayer(Player player) {
        player.teleport(spawnLocation);
        gameAnnouncer.announceGameJoin(player);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void openGame() {
        gameAnnouncer.announceGameOpen(Bukkit.getOnlinePlayers());
        open = true;
    }

    @Override
    public void tick() {
        tickStrategy.tick(this);
    }

    @Override
    public boolean isTickable() {
        return tickStrategy.isReady();
    }

    @Override
    public void startGame() {
        gameAnnouncer.announceGameStart(Bukkit.getOnlinePlayers());
        open = false;
    }

    @Override
    public void abortGame() {
        gameAnnouncer.announceGameAbort(Bukkit.getOnlinePlayers());
        open = false;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public XyLocation getSpawnLocation() {
        return spawnLocation;
    }

    @Override
    public void setSpawnLocation(XyLocation spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}
