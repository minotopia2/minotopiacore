package io.github.xxyy.mtc.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * A special behaviour for a command. Behaviours are applied before execution.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2015-12-30
 */
@FunctionalInterface
public interface CommandBehaviour {

    /**
     * Applies this behaviour to a command execution.
     *
     * @param sender the sender of this command execution
     * @param label  the alias this execution was invoked with
     * @param cmd    the command instance managing the executed command
     * @param args   the arguments passed by the sender, split at space characters
     * @return whether execution should continue
     */
    boolean apply(CommandSender sender, String label, Command cmd, String[] args);
}