package dev.anhcraft.oreprocessor.listener;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.event.OreMineEvent;
import dev.anhcraft.oreprocessor.api.event.OrePickupEvent;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.storage.stats.StatisticHelper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class BlockEventListener implements Listener {
    private final OreProcessor plugin;

    public BlockEventListener(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        if (plugin.mainConfig.whitelistWorlds != null &&
                !plugin.mainConfig.whitelistWorlds.isEmpty() &&
                !plugin.mainConfig.whitelistWorlds.contains(player.getWorld().getName()))
            return;

        if (!plugin.mainConfig.behaviourSettings.processSilkTouchItems &&
                player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;

        Ore ore = OreProcessor.getApi().getBlockOre(UMaterial.of(event.getBlock().getType()));
        if (ore == null) return;

        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        OreData oreData = playerData.getOreData(ore.getId());
        boolean isFull = oreData != null && oreData.isFull();
        Bukkit.getPluginManager().callEvent(new OreMineEvent(player, event.getBlock(), ore, isFull));

        if (!isFull || plugin.mainConfig.behaviourSettings.enableMiningStatOnFullStorage) {
            StatisticHelper.increaseMiningCount(ore.getId(), playerData);
            StatisticHelper.increaseMiningCount(ore.getId(), OreProcessor.getApi().getServerData());
        }

        if (isFull) {
            if (plugin.mainConfig.behaviourSettings.dropOnFullStorage)
                return;

            OreProcessor.getInstance().msg(player, OreProcessor.getInstance().messageConfig.storageFull);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        if (plugin.mainConfig.whitelistWorlds != null &&
                !plugin.mainConfig.whitelistWorlds.isEmpty() &&
                !plugin.mainConfig.whitelistWorlds.contains(player.getWorld().getName()))
            return;

        if (!plugin.mainConfig.behaviourSettings.processSilkTouchItems &&
                player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;

        BlockState brokenBlock = event.getBlockState();
        Ore ore = OreProcessor.getApi().getBlockOre(UMaterial.of(brokenBlock.getType()));
        if (ore == null) return;

        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        OreData oreData = playerData.requireOreData(ore.getId());
        if (oreData.isFull() && plugin.mainConfig.behaviourSettings.dropOnFullStorage)
            return;

        boolean has = false;

        for (Iterator<Item> iterator = event.getItems().iterator(); iterator.hasNext(); ) {
            ItemStack eventItem = iterator.next().getItemStack();
            UMaterial feedstock = OreProcessor.getApi().identifyMaterial(eventItem);
            if (feedstock == null) continue;
            int amount = eventItem.getAmount();

            OrePickupEvent pickupEvent = new OrePickupEvent(player, event.getBlock(), brokenBlock, ore, feedstock, amount);
            pickupEvent.setCancelled(!ore.isAcceptableFeedstock(feedstock));
            Bukkit.getPluginManager().callEvent(pickupEvent);

            if (!pickupEvent.isCancelled()) {
                feedstock = pickupEvent.getFeedstock();
                amount = pickupEvent.getAmount();
                StatisticHelper.increaseFeedstockCount(ore.getId(), amount, playerData);
                StatisticHelper.increaseFeedstockCount(ore.getId(), amount, OreProcessor.getApi().getServerData());
                oreData.addFeedstock(feedstock, amount);
                has = true;
                iterator.remove();
            }
        }

        if (has && !playerData.isTutorialHidden()) {
            for (String msg : OreProcessor.getInstance().messageConfig.firstTimeTutorial) {
                OreProcessor.getInstance().rawMsg(player, msg);
            }
        }
    }
}
