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

package li.l1t.mtc.module.lanatus.base;

import li.l1t.mtc.api.MTCPlugin;
import li.l1t.mtc.api.command.CommandBehaviours;
import li.l1t.mtc.api.module.inject.InjectMe;
import li.l1t.mtc.hook.XLoginHook;
import li.l1t.mtc.module.MTCModuleAdapter;
import li.l1t.mtc.module.lanatus.base.scoreboard.LanatusScoreboardHandler;
import li.l1t.mtc.module.scoreboard.CommonScoreboardProvider;

/**
 * Provides access to the Lanatus API through XYC.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-10-25
 */
public class LanatusBaseModule extends MTCModuleAdapter {
    public static final String READ_PERMISSION = "lanatus.admin.read";
    public static final String ADMIN_PERMISSION = "lanatus.admin.admin";
    public static final String GIVE_PERMISSION = "lanatus.admin.give";
    public static final String RANK_PERMISSION = "lanatus.admin.rank";
    private final MTCLanatusClient client;
    private final XLoginHook xLoginHook;
    @InjectMe(required = false)
    private CommonScoreboardProvider scoreboard;

    @InjectMe
    public LanatusBaseModule(MTCLanatusClient client, XLoginHook xLoginHook) {
        super("LanatusBaseModule", true);
        this.client = client;
        this.xLoginHook = xLoginHook;
    }

    @Override
    public void enable(MTCPlugin plugin) throws Exception {
        super.enable(plugin);
        registerCommand(new LanatusInfoCommand(client, xLoginHook), "lainfo")
                .behaviour(CommandBehaviours.permissionChecking(READ_PERMISSION));
        registerCommand(new LanatusProductCommand(client), "laprod")
                .behaviour(CommandBehaviours.permissionChecking(READ_PERMISSION));
        registerCommand(inject(LanatusGiveCommand.class), "lagive")
                .behaviour(CommandBehaviours.permissionChecking(GIVE_PERMISSION));
        registerCommand(new LanatusRankCommand(client, xLoginHook), "larank")
                .behaviour(CommandBehaviours.permissionChecking(RANK_PERMISSION));
        if(scoreboard != null) {
            LanatusScoreboardHandler scoreboardHandler = new LanatusScoreboardHandler(client, scoreboard);
            registerListener(scoreboardHandler);
            scoreboardHandler.enable();
        }
    }

    @Override
    public void clearCache(boolean forced, MTCPlugin plugin) {
        if(forced) {
            client.clearCache();
        }
    }
}
