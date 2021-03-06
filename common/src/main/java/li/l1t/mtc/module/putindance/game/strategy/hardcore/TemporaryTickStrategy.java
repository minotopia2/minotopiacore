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

package li.l1t.mtc.module.putindance.game.strategy.hardcore;

import li.l1t.common.misc.XyLocation;
import li.l1t.mtc.module.putindance.api.board.Layer;
import li.l1t.mtc.module.putindance.api.game.Game;
import li.l1t.mtc.module.putindance.api.game.InvalidBoardSelectionException;
import li.l1t.mtc.module.putindance.api.game.TickStrategy;
import li.l1t.mtc.module.putindance.game.TickAnnouncer;
import li.l1t.mtc.util.block.BlockPredicates;
import li.l1t.mtc.util.block.BlockTransformers;
import li.l1t.mtc.util.block.RevertableBlockTransformer;
import li.l1t.mtc.util.block.TransformTask;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static li.l1t.common.util.PredicateHelper.not;

/**
 * A tick strategy that announces a single safe colour and removes everything else, just to place it
 * again afterwards.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-10-13
 */
public class TemporaryTickStrategy implements TickStrategy {
    private final Plugin plugin;
    private final long removeDelayTicks;
    private final long revertDelayTicks;
    private final int blocksPerTick;
    private final TickAnnouncer tickAnnouncer;
    private TransformTask currentTask;

    public TemporaryTickStrategy(Plugin plugin, long removeDelayTicks, long revertDelayTicks, int blocksPerTick) {
        this.plugin = plugin;
        this.removeDelayTicks = removeDelayTicks;
        this.revertDelayTicks = revertDelayTicks;
        this.tickAnnouncer = new TickAnnouncer(plugin);
        this.blocksPerTick = blocksPerTick;
    }

    @Override
    public void tick(Game game) {
        Layer layer = game.getBoard().getAllLayers().get(0);
        List<DyeColor> eligibleColors = new ArrayList<>(layer.getActiveColors());
        DyeColor safeColor = selectAndRemoveRandomColorFrom(eligibleColors);
        announceSafeAndRemove(layer, safeColor);
    }

    private DyeColor selectAndRemoveRandomColorFrom(List<DyeColor> eligibleColors) {
        return eligibleColors.remove(RandomUtils.nextInt(eligibleColors.size()));
    }

    private void announceSafeAndRemove(Layer layer, DyeColor safeColor) {
        tickAnnouncer.announceSafeColor(safeColor);
        scheduleBlockRemove(layer, not(BlockPredicates.woolColor(safeColor)));
    }

    private void scheduleBlockRemove(Layer layer, Predicate<Block> filter) {
        RevertableBlockTransformer transformer = BlockTransformers.revertableFiltering()
                .withLocations(layer.getFirstBoundary(), layer.getSecondBoundary())
                .withBlocksPerTick(blocksPerTick)
                .withFilter(filter)
                .withTransformer(block -> block.setType(Material.AIR))
                .build();
        startRemove(transformer, layer);
    }

    private void startRemove(RevertableBlockTransformer transformer, Layer layer) {
        currentTask = transformer.createTransformTask();
        currentTask.withCompletionCallback(() -> startRevert(transformer, layer));
        currentTask.startDelayed(plugin, removeDelayTicks, 1L);
    }

    private void startRevert(RevertableBlockTransformer transformer, Layer layer) {
        currentTask = new TypeIdAndDataReverter(transformer, layer);
        currentTask
                .withCompletionCallback(() -> currentTask = null)
                .startDelayed(plugin, revertDelayTicks, 1L);
    }

    @Override
    public boolean isReady() {
        return currentTask == null;
    }

    @Override
    public void checkBoard(XyLocation firstBoundary, XyLocation secondBoundary) throws InvalidBoardSelectionException {
        if (firstBoundary.getBlockY() != secondBoundary.getBlockY()) {
            throw new InvalidBoardSelectionException(this, "Only single-layer selections are supported!");
        }
    }
}
