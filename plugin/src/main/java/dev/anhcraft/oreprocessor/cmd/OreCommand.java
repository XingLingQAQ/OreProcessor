package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.configdoc.ConfigDocGenerator;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.stats.TimeSeries;
import dev.anhcraft.oreprocessor.config.MainConfig;
import dev.anhcraft.oreprocessor.config.MessageConfig;
import dev.anhcraft.oreprocessor.config.OreConfig;
import dev.anhcraft.oreprocessor.config.UpgradeLevelConfig;
import dev.anhcraft.oreprocessor.gui.GuiRegistry;
import dev.anhcraft.oreprocessor.gui.MenuGui;
import dev.anhcraft.oreprocessor.gui.StorageGui;
import dev.anhcraft.oreprocessor.gui.UpgradeGui;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Objects;

@CommandAlias("ore|oreprocessor")
public class OreCommand extends BaseCommand {
    private final OreProcessor plugin;

    public OreCommand(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @HelpCommand
    @CatchUnknown
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Default
    @Description("Open the menu")
    public void openMenu(Player player) {
        GuiRegistry.openMenuGui(player);
    }

    @Subcommand("docs")
    @CommandPermission("oreprocessor.docs")
    @Description("Generate offline config documentation")
    public void generateDoc(CommandSender sender) {
        new ConfigDocGenerator()
                .withSchemaOf(MainConfig.class)
                .withSchemaOf(OreConfig.class)
                .withSchemaOf(UpgradeLevelConfig.class)
                .withSchemaOf(MessageConfig.class)
                .withSchemaOf(MenuGui.class)
                .withSchemaOf(StorageGui.class)
                .withSchemaOf(UpgradeGui.class)
                .withSchemaOf(ItemBuilder.class)
                .generate(new File(plugin.getDataFolder(), "docs"));
        sender.sendMessage(ChatColor.GREEN + "Configuration documentation generated in plugins/OreProcessor/docs");
    }

    @Subcommand("upgrade throughput set")
    @CommandPermission("oreprocessor.upgrade.throughput.set")
    @CommandCompletion("@players @ores")
    @Description("Set throughput upgrade")
    public void setThroughputUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "The amount must be positive! (or zero to reset)");
            return;
        }
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (!Objects.equals(ore, "*") && OreProcessor.getApi().getOre(ore) == null) {
            sender.sendMessage(ChatColor.RED + "This ore does not exist!");
            return;
        }
        if (!player.isOnline())
            sender.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        if (amount == 0)
            amount = OreProcessor.getApi().getDefaultThroughput();

        int finalAmount = amount;
        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            if (Objects.equals(ore, "*")) {
                for (String oreId : playerData.listOreIds()) {
                    playerData.requireOreData(oreId).setThroughput(finalAmount);
                }
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Set %s's all ore throughput to %d (%d/m)",
                        player.getName(), finalAmount, OreProcessor.getApi().getThroughputPerMinute(finalAmount)
                ));
            } else {
                playerData.requireOreData(ore).setThroughput(finalAmount);
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Set %s's %s throughput to %d (%d/m)",
                        player.getName(), ore, finalAmount, OreProcessor.getApi().getThroughputPerMinute(finalAmount)
                ));
            }
        });
    }

    @Subcommand("upgrade capacity set")
    @CommandPermission("oreprocessor.upgrade.capacity.set")
    @CommandCompletion("@players @ores")
    @Description("Set capacity upgrade")
    public void setCapacityUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "The amount must be positive! (or zero to reset)");
            return;
        }
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (!Objects.equals(ore, "*") && OreProcessor.getApi().getOre(ore) == null) {
            sender.sendMessage(ChatColor.RED + "This ore does not exist!");
            return;
        }
        if (!player.isOnline())
            sender.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        if (amount == 0)
            amount = OreProcessor.getApi().getDefaultCapacity();

        int finalAmount = amount;
        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            if (Objects.equals(ore, "*")) {
                for (String oreId : playerData.listOreIds()) {
                    playerData.requireOreData(oreId).setCapacity(finalAmount);
                }
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Set %s's all ores capacity to %d",
                        player.getName(), finalAmount
                ));
            } else {
                playerData.requireOreData(ore).setCapacity(finalAmount);
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Set %s's %s capacity to %d",
                        player.getName(), ore, finalAmount
                ));
            }
        });
    }

    @Subcommand("stats server all")
    @CommandPermission("oreprocessor.stats.server.all")
    @Description("Display cumulative server stats")
    public void statsServerAll(CommandSender sender, String oreQuery) {
        displayStats(sender, oreQuery, "&cServer", TimeSeries.parseAll(OreProcessor.getApi().getServerData(), oreQuery));
    }

    @Subcommand("stats player all")
    @CommandPermission("oreprocessor.stats.player.all")
    @Description("Display cumulative player stats")
    public void statsPlayerAll(CommandSender sender, OfflinePlayer player, String oreQuery) {
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (!player.isOnline())
            sender.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            displayStats(sender, oreQuery, player.getName(), TimeSeries.parseAll(playerData, oreQuery));
        });
    }

    @Subcommand("reload")
    @CommandPermission("oreprocessor.reload")
    @Description("Reload the plugin")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "OreProcessor reloaded!");
    }

    private void displayStats(CommandSender sender, String oreQuery, String target, TimeSeries timeSeries) {
        for (String s : plugin.messageConfig.statisticAllDetails) {
            plugin.rawMsg(sender, s.replace("{ore-query}", oreQuery)
                    .replace("{target}", target)
                    .replace("{total-mined}", Long.toString(timeSeries.getMiningCount()))
                    .replace("{total-feedstock}", Long.toString(timeSeries.getFeedstockCount()))
                    .replace("{total-products}", Long.toString(timeSeries.getProductCount())));
        }
    }
}
