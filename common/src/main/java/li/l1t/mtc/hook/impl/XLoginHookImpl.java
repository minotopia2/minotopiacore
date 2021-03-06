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

package li.l1t.mtc.hook.impl;

import li.l1t.mtc.hook.HookWrapper;
import li.l1t.mtc.hook.Hooks;
import li.l1t.mtc.hook.XLoginHook;
import li.l1t.xlogin.common.PreferencesHolder;
import li.l1t.xlogin.common.api.SpawnLocationHolder;
import li.l1t.xlogin.common.api.XLoginProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of xLogin hook which contains unsafe statements.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 9.6.14
 */
public final class XLoginHookImpl implements Hook {
    private Location spawnLocation = null;
    private boolean hooked = false;

    @Override
    public boolean canHook(HookWrapper wrapper) {
        return Hooks.isPluginLoaded(wrapper, "xLogin_Spigot");
    }

    @Override
    public void hook(HookWrapper wrapper) {
        wrapper.getPlugin().getLogger().info("Hooked xLogin using " + PreferencesHolder.getConsumer().getClass().getName() + "!"); //Ensures that the class is loaded
        hooked = true;
    }

    @Override
    public boolean isHooked() {
        return hooked;
    }

    public boolean isAuthenticated(UUID uuid) {
        return PreferencesHolder.getConsumer().getRegistry().isAuthenticated(uuid);
    }

    public Location getSpawnLocation() {
        if (spawnLocation == null) {
            spawnLocation = new Location(Bukkit.getWorld(SpawnLocationHolder.getWorldName()),
                    SpawnLocationHolder.getX(),
                    SpawnLocationHolder.getY(),
                    SpawnLocationHolder.getZ(),
                    SpawnLocationHolder.getPitch(),
                    SpawnLocationHolder.getYaw());
        }

        return spawnLocation;
    }

    public void resetSpawnLocation() {
        this.spawnLocation = null;
    }

    public List<XLoginHook.Profile> getProfiles(String name) {
        List<? extends XLoginProfile> profiles = PreferencesHolder.getConsumer().getRepository().getProfiles(name);

        return profiles.stream()
                .map(XLoginProfileProxy::new)
                .collect(Collectors.toList());
    }

    public XLoginHook.Profile getProfile(UUID uuid) {
        XLoginProfile profile = PreferencesHolder.getConsumer().getRepository().getProfile(uuid);
        return profile == null ? null : new XLoginProfileProxy(profile);
    }

    public String getName(UUID uuid) {
        return PreferencesHolder.getConsumer().getRepository().getName(uuid);
    }

    private class XLoginProfileProxy implements XLoginHook.Profile {
        private final XLoginProfile profile;

        private XLoginProfileProxy(XLoginProfile profile) {
            this.profile = profile;
        }

        @Override
        public boolean isPremium() {
            return profile.isPremium();
        }

        @Override
        public String getName() {
            return profile.getName();
        }

        @Override
        public UUID getUniqueId() {
            return profile.getUniqueId();
        }

        @Override
        public String getLastIp() {
            return profile.getLastIp();
        }

        public XLoginProfile getProfile() {
            return profile;
        }
    }
}
