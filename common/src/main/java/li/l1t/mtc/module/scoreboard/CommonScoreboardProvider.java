/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.scoreboard;

import com.google.common.base.Preconditions;
import li.l1t.mtc.api.module.inject.InjectMe;
import li.l1t.mtc.util.ScoreboardHelper;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Scoreboards shown by the PvP Stats module, handling displaying and updating.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-04
 */
public class CommonScoreboardProvider extends ScoreboardHelper {
    public static final String OBJECTIVE_NAME = "mtc-side";
    private final List<String> duplicateItemPrefixes = Arrays.asList("§0§f", "§1§f", "§2§f", "§3§f", "§4§f",
            "§5§f", "§6§f", "§7§f", "§8§f", "§9§f", "§a§f", "§b§f", "§c§f", "§d§f", "§e§f", "§f§f");
    private final Map<String, BoardItem> globalItems = new HashMap<>();
    private final Set<UUID> objectiveExistingPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<UUID> boardHiddenPlayers = new HashSet<>();

    @InjectMe
    public CommonScoreboardProvider(Plugin plugin) {
        super(plugin);
    }

    public void hideBoardFor(Player player) {
        Preconditions.checkNotNull(player, "player");
        boardHiddenPlayers.add(player.getUniqueId());
        if(objectiveExistingPlayers.remove(player.getUniqueId())) {
            removeObjective(player, OBJECTIVE_NAME);
        }
    }

    public void unhideBoardFor(Player player) {
        Preconditions.checkNotNull(player, "player");
        boardHiddenPlayers.remove(player.getUniqueId());
        updateScoreboardFor(player);
    }

    public void registerBoardItem(String key, BoardItem item) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(item, "item");
        globalItems.put(key, item);
    }

    public void cleanUp(Player player) {
        objectiveExistingPlayers.remove(player.getUniqueId());
        boardHiddenPlayers.remove(player.getUniqueId());
    }

    public void updateScoreboardFor(Player player) {
        if (boardHiddenPlayers.contains(player.getUniqueId())) {
            return;
        }
        prepareObjectiveFor(player);
        int i = 0;
        Set<String> usedItemNames = new HashSet<>(globalItems.size());
        for (BoardItem item : globalItems.values()) {
            if (item.isVisibleTo(player)) {
                String uniqueValue = makeItemNameUnique(item.getValue(player), usedItemNames);
                updateScore(player, OBJECTIVE_NAME, uniqueValue, ++i);
                updateScore(player, OBJECTIVE_NAME, item.getDisplayName(player), ++i);
            }
        }
    }

    private void prepareObjectiveFor(Player player) {
        if (!objectiveExistingPlayers.add(player.getUniqueId())) { //may produce a client NPE on reload - Mojang logs & discards that
            removeObjective(player, OBJECTIVE_NAME);
        }
        if (player.getScoreboard() == null) {
            player.setScoreboard(getPlugin().getServer().getScoreboardManager().getMainScoreboard());
        }
        createIntObjective(player, OBJECTIVE_NAME, "§e§l " + player.getName(), DisplaySlot.SIDEBAR);
    }

    private String makeItemNameUnique(String name, Set<String> usedNames) {
        while (!usedNames.add(name)) {
            name = duplicateItemPrefixes.get(RandomUtils.nextInt(duplicateItemPrefixes.size())) + name;
        }
        return name;
    }
}
