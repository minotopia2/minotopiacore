package io.github.xxyy.minotopiacore.hook;

import org.bukkit.plugin.Plugin;

/**
 * A simple implementation of the HookWrapper interface without any actual functionality.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 9.6.14
 */
public class SimpleHookWrapper implements HookWrapper {
    private final Plugin plugin;
    private boolean active;

    public SimpleHookWrapper(Plugin plugin, boolean active) {
        this.plugin = plugin;
        this.active = active;
    }

    public SimpleHookWrapper(Plugin plugin) {
        this(plugin, true);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    protected void setActive(boolean active) {
        this.active = active;
    }
}
