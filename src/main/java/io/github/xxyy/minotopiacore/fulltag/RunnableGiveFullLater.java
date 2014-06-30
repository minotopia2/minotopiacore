package io.github.xxyy.minotopiacore.fulltag;

import io.github.xxyy.minotopiacore.LogHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

import java.util.logging.Level;


public final class RunnableGiveFullLater implements Runnable {
    private final boolean thorns;
    private final byte partId;
    private final boolean ignoreItemType;
    private final CommandSender sender;
    private final String recName;
    private final String comment;
    private final Inventory inv;

    public RunnableGiveFullLater(boolean thorns, byte partId, boolean ignoreItemType, CommandSender sender, String recName, String comment, Inventory inv) {
        this.thorns = thorns;
        this.partId = partId;
        this.ignoreItemType = ignoreItemType;
        this.sender = sender;
        this.recName = recName;
        this.comment = comment;
        this.inv = inv;
    }

    @Override
    public void run() {
        LogHelper.getFullLogger().log(Level.INFO, "Trying again for " + this.recName + " (by " + this.sender.getName() + ")");
        FullTagHelper.tryGiveFull(this.thorns, this.partId, this.ignoreItemType, this.sender, this.recName, this.comment, this.inv); //TODO database persistance
    }
}
