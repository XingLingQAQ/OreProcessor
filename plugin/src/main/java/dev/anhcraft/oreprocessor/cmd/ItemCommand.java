package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("ore|oreprocessor")
public class ItemCommand extends BaseCommand {
    private final OreProcessor plugin;

    public ItemCommand(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @HelpCommand
    @CatchUnknown
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("item give")
    @CommandPermission("oreprocessor.item.give")
    @Description("Give an item to the inventory (or drop if full)")
    @CommandCompletion("@materials @range:1-5 @players")
    public void giveItem(CommandSender sender, String material, int amount, @Optional OnlinePlayer player) {
        Player target = player != null ? player.getPlayer() : null;
        if (target == null) {
            if (sender instanceof Player)
                target = (Player) sender;
            else {
                sender.sendMessage(ChatColor.RED + "You must specify a player or you must be in-game!");
                return;
            }
        }

        UMaterial uMaterial = UMaterial.parse(material);
        if (uMaterial == null) {
            sender.sendMessage(ChatColor.RED + "Invalid material!");
            return;
        }

        ItemStack item = OreProcessor.getApi().buildItem(uMaterial, amount);
        if (item == null || ItemUtil.isEmpty(item)) return;

        ItemUtil.addToInventory(target, item);
    }
}
