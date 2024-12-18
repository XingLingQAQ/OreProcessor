package dev.anhcraft.oreprocessor.listener;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.handler.ProcessingPlant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;

public class BlockEventListener implements Listener {
    private final OreProcessor plugin;

    public BlockEventListener(OreProcessor plugin) {
        this.plugin = plugin;
    }

    // NOTE: We expect that this event is fired for all integrations (vanilla, custom block plugin)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBreak(BlockBreakEvent event) {
        if (plugin.processingPlant.fireOnMine(event.getPlayer(), event.getBlock()) == ProcessingPlant.Result.DENY) {
            event.setCancelled(plugin.processingPlant.fireOnMine(event.getPlayer(), event.getBlock()) == ProcessingPlant.Result.DENY);
        }
    }

    // There could be multiple event listeners with priority HIGHEST changing the setCancelled
    // We can only ensure the event is CANCELLED on priority MONITOR
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBreakMonitor(BlockBreakEvent event) {
        if (canVanillaBlocksContainExtraDrops()) {
            plugin.processingPlant.scheduleLootDropCollector(event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onDrop(BlockDropItemEvent event) {
        // If drop collector is already required, the following job would be delegated to the collector later on
        if (canVanillaBlocksContainExtraDrops())
            return;

        // here we will collect the drop; any drop that is collected would not be thrown into the world
        // it is compatible with 3rd-party plugins that call ProcessingPlant#scheduleLootDropCollector
        plugin.processingPlant.collectLoot(event.getPlayer(), event.getBlockState(), event.getItems());
    }

    // The drop collector is required when there is additional drops to vanilla blocks
    // (and that plugin does not provide API to monitor that)
    // If we rely on BlockDropItemEvent on that case, additional drops would be missed
    private boolean canVanillaBlocksContainExtraDrops() {
        return plugin.integrationManager.hasIntegration("ItemsAdder") ||
          plugin.integrationManager.hasIntegration("Oraxen") ||
          plugin.integrationManager.hasIntegration("AdvancedEnchantments");
    }
}
