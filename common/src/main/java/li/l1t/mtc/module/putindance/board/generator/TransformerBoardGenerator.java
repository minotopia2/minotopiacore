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

package li.l1t.mtc.module.putindance.board.generator;

import com.google.common.base.Preconditions;
import li.l1t.common.misc.XyLocation;
import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.putindance.api.board.Board;
import li.l1t.mtc.module.putindance.api.board.Layer;
import li.l1t.mtc.module.putindance.api.board.generator.BoardGenerator;
import li.l1t.mtc.module.putindance.api.board.generator.GenerationStrategy;
import li.l1t.mtc.module.putindance.board.SimpleBoard;
import li.l1t.mtc.util.block.BlockTransformer;
import li.l1t.mtc.util.block.BlockTransformers;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Generates PutinDance boards using a {@link BlockTransformer}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-09-21
 */
public class TransformerBoardGenerator implements BoardGenerator {
    private static final Logger LOGGER = LogManager.getLogger(TransformerBoardGenerator.class);
    private final GenerationStrategy strategy;
    private final BlockTransformer transformer;
    private final Map<Integer, Layer> layersByYLevel = new HashMap<>();
    private Consumer<Board> callback = noop -> {
    };
    private Board board;

    public TransformerBoardGenerator(XyLocation firstBoundary, XyLocation secondBoundary,
                                     GenerationStrategy strategy, int blocksPerTick) {
        initializeBoard(firstBoundary, secondBoundary);
        this.strategy = strategy;
        this.transformer = BlockTransformers.basic()
                .withLocations(firstBoundary, secondBoundary)
                .withTransformer(this::transformBlock)
                .withBlocksPerTick(blocksPerTick)
                .build();
    }

    private void initializeBoard(XyLocation firstBoundary, XyLocation secondBoundary) {
        this.board = new SimpleBoard(firstBoundary, secondBoundary);
        new LayerCreator().addLayersTo(board);
        board.getAllLayers().forEach(layer -> layersByYLevel.put(layer.getYLevel(), layer));
    }

    private void transformBlock(Block block) {
        Layer layer = layersByYLevel.get(block.getY());
        if (layer == null) {
            LOGGER.warn("Unknown y layer for block {}?!", block); //this should not happen
            return;
        }
        strategy.generateAt(layer, block);
    }

    @Override
    public void startGeneration(Plugin plugin, long delayBetweenExecutionsTicks) {
        transformer.createTransformTask()
                .withCompletionCallback(() -> callback.accept(board))
                .start(plugin, delayBetweenExecutionsTicks);
    }

    @Override
    public GenerationStrategy getGenerationStrategy() {
        return strategy;
    }

    @Override
    public void setCompletionCallback(Consumer<Board> callback) {
        Preconditions.checkNotNull(callback, "callback");
        this.callback = callback;
    }
}
