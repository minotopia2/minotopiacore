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

package li.l1t.mtc.module.vote.api;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

/**
 * Provides access to the queue of votes that have been queued for players who were offline at the time the vote was
 * received.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-12-28
 */
public interface VoteQueue {
    void queueVote(Vote vote);

    Collection<UUID> findQueuedVotes(String userName);

    void deleteVoteFromQueue(Vote vote);

    void purgeVotesOlderThan(Duration duration);
}
