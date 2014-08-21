package io.github.xxyy.mtc.test.cmd;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.xxyy.common.test.util.MockHelper;
import io.github.xxyy.mtc.MTC;
import io.github.xxyy.mtc.misc.cmd.CommandMTC;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests some testable parts of the /mtc command.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 5.7.14
 */
public class CommandMTCTest {
    private static final Server SERVER = MockHelper.mockServer();
    private static CommandMTC commandMTC;
    private static Player fakeSender;
    private static Command fakeCommand = mock(Command.class);

    @BeforeClass
    public static void init() {
        MTC mtc = mock(MTC.class);
        FileConfiguration cfg = mock(FileConfiguration.class);
        when(cfg.getBoolean(contains("enable"))).thenReturn(true);
        when(mtc.getConfig()).thenReturn(cfg);
        commandMTC = new CommandMTC(mtc);

        fakeSender = MockHelper.mockPlayer(UUID.randomUUID(), "sender");
        when(fakeSender.hasPermission(any(String.class))).thenReturn(true);
    }

    @Test
    public void testFakeMessage() {
        Player otherPlayer = MockHelper.mockPlayer(UUID.randomUUID(), "other");
        when(otherPlayer.hasPermission(any(String.class))).thenReturn(false);

        when(SERVER.getOnlinePlayers()).thenReturn(new Player[]{fakeSender, otherPlayer});
        commandMTC.catchCommand(fakeSender, null, fakeCommand, "mtc", new String[]{"fm", "&6wowe"});

        verify(fakeSender).sendMessage(eq("§7(/mtc fm|" + fakeSender.getName() + ")§f §6wowe"));
        verify(otherPlayer).sendMessage(eq("§6wowe"));
    }
}