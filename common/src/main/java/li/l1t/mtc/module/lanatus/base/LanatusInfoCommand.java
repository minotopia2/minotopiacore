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

import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.common.command.BukkitExecution;
import li.l1t.common.exception.UserException;
import li.l1t.lanatus.api.LanatusClient;
import li.l1t.lanatus.api.account.AccountSnapshot;
import li.l1t.lanatus.api.position.Position;
import li.l1t.lanatus.api.product.Product;
import li.l1t.lanatus.api.purchase.Purchase;
import li.l1t.mtc.command.MTCExecutionExecutor;
import li.l1t.mtc.hook.XLoginHook;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static li.l1t.mtc.api.chat.MessageType.*;

/**
 * Executes the /lainfo command, providing information about the Lanatus accounts of a provided
 * player.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-10-25
 */
class LanatusInfoCommand extends MTCExecutionExecutor {
    private final LanatusClient client;
    private final XLoginHook xLogin;

    public LanatusInfoCommand(LanatusClient client, XLoginHook xLogin) {
        this.client = client;
        this.xLogin = xLogin;
    }

    @Override
    public boolean execute(BukkitExecution exec) {
        if (exec.hasNoArgs()) {
            exec.requireIsPlayer();
            showAccountInfo(exec, profile(exec.senderId()));
            return true;
        }
        switch (exec.arg(0).toLowerCase()) {
            case "plist":
                showPurchaseList(exec, argumentProfile(exec.arg(1), exec.sender()));
                return true;
            case "ilist":
                showPositionList(exec, argumentProfile(exec.arg(1), exec.sender()));
                return true;
            case "purchase":
                showPurchaseDetails(exec, exec.uuidArg(1));
                return true;
            case "refresh":
                handleClearPlayerCache(exec, exec.uuidArg(1));
                return true;
            case "help":
                break;
            default:
                showAccountInfo(exec, argumentProfile(exec.arg(0), exec.sender()));
                return true;
        }
        showUsage(exec);
        return true;
    }

    private XLoginHook.Profile profile(UUID playerId) {
        XLoginHook.Profile profile = xLogin.getProfile(playerId);
        if (profile == null) {
            throw new UserException("Kein Spieler mit der UUID %s bekannt", playerId);
        }
        return profile;
    }

    private XLoginHook.Profile argumentProfile(String input, CommandSender sender) {
        return xLogin.findSingleMatchingProfileOrFail(
                input, sender, profile -> String.format("/lainfo %s", profile.getUniqueId())
        );
    }

    private void showAccountInfo(BukkitExecution exec, XLoginHook.Profile profile) {
        AccountSnapshot account = client.accounts().findOrDefault(profile.getUniqueId());
        exec.respond(HEADER, "Lanatus-Info: §a%s", profile.getName());
        exec.respond(RESULT_LINE, "UUID: §s%s", account.getPlayerId());
        exec.respond(RESULT_LINE, "Melonen: §s%s  §pRang: §s%s", account.getMelonsCount(), account.getLastRank());
        respondSnapshotInstantAndAction(exec, account);
        respondAccountActions(exec, profile);
    }

    private void respondSnapshotInstantAndAction(BukkitExecution exec, AccountSnapshot account) {
        exec.respond(resultLineBuilder()
                .append("Stand: ")
                .append(readableInstant(account.getSnapshotInstant()), ChatColor.GREEN)
                .append("  ", ChatColor.DARK_RED)
                .append("[Aktualisieren]")
                .hintedCommand("/lainfo refresh " + account.getPlayerId())
        );
    }

    private String readableInstant(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private String readableInstantDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void respondAccountActions(BukkitExecution exec, XLoginHook.Profile profile) {
        exec.respond(resultLineBuilder()
                .append("[Käufe]", ChatColor.DARK_GREEN)
                .hintedCommand("/lainfo plist " + profile.getUniqueId())
                .append("  ")
                .append("[Items]", ChatColor.DARK_PURPLE)
                .hintedCommand("/lainfo ilist " + profile.getUniqueId())
                .append("  ", ChatColor.YELLOW)
                .appendIf(exec.sender().hasPermission(LanatusBaseModule.GIVE_PERMISSION), "[Melonen geben]")
                .suggest("/lagive " + profile.getUniqueId() + " ").tooltip("Klicken, um \nMelonen zu geben")
                .append("  ", ChatColor.RED)
                .appendIf(exec.sender().hasPermission(LanatusBaseModule.RANK_PERMISSION), "[Rang setzen]")
                .suggest("/larank " + profile.getUniqueId() + " ").tooltip("Klicken, um \nRang zu setzen")
        );
    }

    private void showPurchaseList(BukkitExecution exec, XLoginHook.Profile profile) {
        Collection<Purchase> purchases = client.purchases().findByPlayer(profile.getUniqueId());
        exec.respond(LIST_HEADER, "%d Käufe von §a%s:", purchases.size(), profile.getName());
        purchases.forEach(purchase -> showPurchaseListItem(exec, purchase));
    }

    private void showPurchaseListItem(BukkitExecution exec, Purchase purchase) {
        exec.respond(appendProductOverview(listItemBuilder(), purchase.getProduct())
                .appendIf(client.positions().findByPurchase(purchase.getUniqueId()).isPresent(), " (aktiv)", ChatColor.YELLOW)
                .append(" am ", ChatColor.GOLD, ComponentBuilder.FormatRetention.NONE).underlined(false)
                .append(readableInstantDate(purchase.getCreationInstant()), ChatColor.GREEN)
                .append(" ")
                .append("[Details]", ChatColor.DARK_GREEN).underlined(true)
                .hintedCommand("/lainfo purchase " + purchase.getUniqueId())
        );
    }

    private void showPurchaseDetails(BukkitExecution exec, UUID purchaseId) {
        Purchase purchase = client.purchases().findById(purchaseId);
        String playerName = xLogin.getDisplayString(purchase.getPlayerId());
        exec.respond(HEADER, "Kauf %s", purchaseId);
        exec.respond(RESULT_LINE, "Kaufdatum: §s%s", readableInstant(purchase.getCreationInstant()));
        exec.respond(RESULT_LINE, "§pKaufpreis: §s%s§p Melonen (Käufer: §s%s§p)", purchase.getMelonsCost(), playerName);
        exec.respond(RESULT_LINE, "Daten: §s%s", purchase.getData());
        exec.respond(RESULT_LINE, "Anmerkung: §s%s", purchase.getComment());
        exec.respond(appendProductOverview(resultLineBuilder().append("Produkt: ", ChatColor.GOLD), purchase.getProduct()));
        Optional<Position> position = client.positions().findByPurchase(purchaseId);
        if (position.isPresent()) {
            exec.respond(RESULT_LINE, "Itemdaten: '%s'", position.get().getData());
        } else {
            exec.respond(RESULT_LINE, "Zu diesem Kauf gibt es aktuell kein Item.");
        }
    }

    private XyComponentBuilder appendProductOverview(XyComponentBuilder builder, Product product) {
        builder.append(product.getDisplayName(), ChatColor.GREEN).underlined(true)
                .hintedCommand("/laprod info " + product.getUniqueId())
                .append("", ChatColor.GOLD).underlined(false);
        return builder;
    }

    private void showPositionList(BukkitExecution exec, XLoginHook.Profile profile) {
        Collection<Position> positions = client.positions().findAllByPlayer(profile.getUniqueId());
        exec.respond(LIST_HEADER, "§a%s §pbesitzt %d Items:", profile.getName(), positions.size());
        positions.forEach(position -> showPositionListItem(exec, position));
    }

    private void showPositionListItem(BukkitExecution exec, Position position) {
        XyComponentBuilder builder = listItemBuilder();
        appendProductOverview(builder, position.getProduct());
        exec.respond(builder
                .append(" mit Daten '", ChatColor.GOLD)
                .append(position.getData(), ChatColor.GREEN)
                .append("' ")
                .append("[Kauf]", ChatColor.DARK_GREEN)
                .hintedCommand("/lainfo purchase " + position.getPurchaseId())
        );
    }

    private void handleClearPlayerCache(BukkitExecution exec, UUID uuid) {
        client.clearCachesFor(uuid);
        exec.respond(RESULT_LINE_SUCCESS, "Cache für den Spieler mit der UUID §s%s§p geleert.", uuid);
    }

    private void showUsage(BukkitExecution exec) {
        exec.respondUsage("plist", "<Spieler|UUID>", "Zeigt Käufe.");
        exec.respondUsage("purchase", "<UUID>", "Zeigt einen Kauf.");
        exec.respondUsage("ilist", "<Spieler|UUID>", "Zeigt aktuelle Positionen.");
        exec.respondUsage("", "[Spieler|UUID]", "Zeigt Infos zu (d)einem Account.");
    }
}
