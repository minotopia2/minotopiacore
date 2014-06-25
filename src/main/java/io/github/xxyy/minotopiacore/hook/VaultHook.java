package io.github.xxyy.minotopiacore.hook;

import io.github.xxyy.minotopiacore.hook.impl.VaultHookImpl;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

/**
 * Helps interfacing with Vault.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 9.6.14
 */
public class VaultHook extends SimpleHookWrapper {
    private VaultHookImpl unsafe;

    public VaultHook(Plugin plugin) {
        super(plugin);

        unsafe = Hooks.tryHook(this);
    }

    public boolean assureHasAccount(OfflinePlayer offlinePlayer) {
        return isEconomyHooked() && Hooks.Unsafe.safeCall(unsafe::assureHasAccount, offlinePlayer, false, null);
    }

    public double getBalance(OfflinePlayer offlinePlayer) {
        if(!isEconomyHooked()) {
            throw new IllegalStateException("Economy not hooked!");
        }

        return Hooks.Unsafe.safeCall(unsafe::getBalance, offlinePlayer, 0D, null);
    }

    public String getPlayerPrefix(OfflinePlayer offlinePlayer) {
        return isChatHooked() ? Hooks.Unsafe.safeCall(unsafe::getPlayerPrefix, offlinePlayer, "§c§o[ERR]", null) : "§c[ERR]";
    }

    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) { //FIXME Will throw CNFE if no Vault loaded - shade?
        if(!isEconomyHooked()) {
            throw new IllegalStateException("Economy not hooked!");
        }

        return unsafe.depositPlayer(offlinePlayer, amount);
    }

    //// ACTIVITY METHODS //////////////////////////////////////////////////////////////////////////////////////////////

    public boolean arePermissionsHooked() {
        return isActive() && unsafe.getPermission() != null;
    }

    public boolean isChatHooked() {
        return isActive() && unsafe.getChat() != null;
    }

    public boolean isEconomyHooked() {
        return isActive() && unsafe.getEconomy() != null;
    }

    @Override
    public boolean isActive() {
        return unsafe != null;
    }
}
