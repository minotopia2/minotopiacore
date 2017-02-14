/*
 * Copyright (c) 2013-2017.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.blocklock.listener;

import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.api.module.inject.InjectMe;
import li.l1t.mtc.module.blocklock.api.BlockLock;
import li.l1t.mtc.module.blocklock.service.BlockLockService;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

/**
 * Creates locks when targeted materials are placed.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-02-08
 */
public class BlockLockPlaceBreakListener implements Listener {
    private final BlockLockService lockService;
    private final MTCPlugin plugin;

    @InjectMe
    public BlockLockPlaceBreakListener(BlockLockService lockService, MTCPlugin plugin) {
        this.lockService = lockService;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        Player player = event.getPlayer();
        if (lockService.isLockable(placedBlock)) {
            plugin.async(() -> {
                lockService.addLockTo(placedBlock, player);
                MessageType.RESULT_LINE_SUCCESS.sendTo(player,
                        "Dieser Block ist geschützt. Du kannst ihn später zerstören.");
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        /*
        Needs to be on HIGHEST to catch cancellations at lower priorities - we can't reliably catch cancellations
        of same-priority listeners though, because we need to cancel the event ourselves in order to perform the
        lock check async.
        Doing computations async would make everything much more complicated, so we're trying how much of an performance
        impact this has for now.
         */
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Optional<BlockLock> lock = lockService.findLock(block);
        if (lock.isPresent()) {
            event.setCancelled(true);
            lockService.destroyLockAndRefund(block, player, lock.get());
        }
    }
}