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

package li.l1t.mtc.module.fulltag.model;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores metadata related to a full item. This represents the state of the item at the time of
 * creation, that means that the owner is the initial owner. Upon a map reset, the data stored in
 * this class persists. While this class stores the metadata for a full item, {@link FullInfo}
 * stores data relevant to the actual item in game. Note that it is not guaranteed that this full
 * item actually exists in game. that means, that it has a corresponding {@link FullInfo}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/08/15
 */
public class FullData {
    private final int id;
    private final LocalDateTime creationTime;
    private final UUID senderId;
    private final UUID receiverId;
    private final FullPart part;
    private final boolean thorns;
    private UUID holderId;
    private boolean modified = false;
    private String comment;
    private boolean valid = true;

    protected FullData(int id, LocalDateTime creationTime, String comment, UUID senderId, UUID receiverId, FullPart part,
                       boolean thorns, UUID holderId) {
        this.id = id;
        this.creationTime = creationTime;
        this.comment = comment;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.part = part;
        this.thorns = thorns;
        this.holderId = holderId;
    }

    /**
     * @return the unique integer identifier of this full item
     */
    public int getId() {
        return id;
    }

    /**
     * @return the date and time this full item was created at
     */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * @return a piece of free text, specifying additional commentary for this item
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment string associated with this full item.
     *
     * @param comment a piece of free text, specifying additional commentary for this item
     */
    public void setComment(@Nonnull String comment) {
        Preconditions.checkNotNull(comment);
        if (comment.equalsIgnoreCase(this.comment)) {
            return;
        }
        this.comment = comment;
        modified = true;
    }

    public UUID getHolderId() {
        return holderId;
    }

    /**
     * @return the part specification for this full item
     */
    public FullPart getPart() {
        return part;
    }

    /**
     * @return whether this full item has the Thorns enchantment
     */
    public boolean isThorns() {
        return thorns;
    }

    /**
     * @return the unique id of the player who created this full item
     */
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * @return the unique id of the player whom this full item was created for
     */
    public UUID getReceiverId() {
        return receiverId;
    }

    /**
     * @return whether the stored metadata has local changes
     */
    public boolean isModified() {
        return modified;
    }

    protected void resetModified() {
        modified = false;
    }

    /**
     * @return whether this object is still valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the validity state of this full item metadata.
     *
     * @param valid whether this object is still valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullData)) return false;

        FullData fullData = (FullData) o;

        return id == fullData.id && comment.equals(fullData.comment);

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Nonnull
    @Override
    public String toString() {
        return "FullData{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", part=" + part +
                ", thorns=" + thorns +
                ", comment='" + comment + '\'' +
                ", modified=" + modified +
                ", valid=" + valid +
                ", holderId=" + holderId +
                '}';
    }


}
