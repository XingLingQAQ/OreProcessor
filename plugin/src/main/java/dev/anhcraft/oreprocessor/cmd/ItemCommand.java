package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
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
    @CommandCompletion("@players @materials")
    public void giveItem(CommandSender sender, Player player, String material, int amount) {
        UMaterial uMaterial = UMaterial.parse(material);
        if (uMaterial == null) {
            sender.sendMessage(ChatColor.RED + "Invalid material!");
            return;
        }

        ItemStack item = OreProcessor.getApi().buildItem(uMaterial, amount);
        if (item == null || ItemUtil.isEmpty(item)) return;

        ItemUtil.addToInventory(player, item);
    }
}
