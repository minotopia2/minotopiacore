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

package li.l1t.mtc.module.vote.service;

import com.google.common.base.Preconditions;
import li.l1t.mtc.api.chat.MessageType;
import li.l1t.mtc.api.module.inject.InjectMe;
import li.l1t.mtc.logging.LogManager;
import li.l1t.mtc.module.vote.api.Vote;
import li.l1t.mtc.module.vote.api.VoteQueue;
import li.l1t.mtc.module.vote.api.VoteRepository;
import li.l1t.mtc.module.vote.reward.loader.RewardConfig;
import li.l1t.mtc.module.vote.reward.loader.RewardConfigs;
import li.l1t.mtc.module.vote.sql.queue.SqlVoteQueue;
import li.l1t.mtc.module.vote.sql.vote.SqlVote;
import li.l1t.mtc.module.vote.sql.vote.SqlVoteRepository;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Service that handles votes.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-01-12
 */
public class VoteService {
    private static final Logger LOGGER = LogManager.getLogger(VoteService.class);
    private final VoteRepository votes;
    private final VoteQueue voteQueue;
    private final RewardConfigs rewards;

    @InjectMe
    public VoteService(SqlVoteRepository voteRepository, SqlVoteQueue voteQueue, RewardConfigs rewards) {
        this.votes = voteRepository;
        this.voteQueue = voteQueue;
        this.rewards = rewards;
    }

    public void handleVote(String username, String serviceName) {
        UUID uuid = null;
        Player onlinePlayer = Bukkit.getPlayerExact(username);
        if (onlinePlayer != null) {
            uuid = onlinePlayer.getUniqueId();
        }
        SqlVote vote = votes.createVote(username, serviceName, uuid);
        if (onlinePlayer != null) {
            dispatchVoteReward(onlinePlayer, vote);
        } else {
            LOGGER.info("Queueing vote: {}", vote);
            voteQueue.queueVote(vote);
        }
    }

    public void dispatchVoteReward(Player player, Vote vote) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(vote, "vote");
        Optional<RewardConfig> config = rewards.findConfig(vote.getServiceName());
        setAndSavePlayerIdIfNotPresent(player, vote);
        if (config.isPresent()) {
            LOGGER.info("Rewarding {} for {}...", player.getName(), vote);
            config.get().getRewards()
                    .forEach(reward -> reward.apply(player, vote));
        } else {
            LOGGER.warn("Received vote for unknown service: {}", vote);
            MessageType.INTERNAL_ERROR.sendTo(player,
                    "Für diesen Votelink ('%s') ist keine Belohnung festgelegt. Bitte wende dich an den Support.",
                    vote.getServiceName()
            );
        }
    }

    private void setAndSavePlayerIdIfNotPresent(Player player, Vote vote) {
        if (!vote.hasPlayerId()) {
            vote.setPlayerId(player.getUniqueId());
            votes.save(vote);
        }
    }

    public void checkQueuedVotes(Player player, Consumer<Vote> resultConsumer) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(resultConsumer, "resultConsumer");
        Collection<UUID> voteIds = voteQueue.findQueuedVotes(player.getName());
        voteIds.stream()
                .map(votes::findVoteById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(voteQueue::deleteVoteFromQueue)
                .forEach(resultConsumer);
    }
}
