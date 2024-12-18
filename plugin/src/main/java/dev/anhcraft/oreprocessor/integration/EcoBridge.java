package dev.anhcraft.oreprocessor.integration;

import com.willfp.eco.core.events.DropQueuePushEvent;
import dev.anhcraft.oreprocessor.OreProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EcoBridge implements Integration, Listener, EventDebugger {
    private final OreProcessor plugin;

    public EcoBridge(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onDropLoot(DropQueuePushEvent event) {
        Player player = event.getPlayer();
        if (event.isTelekinetic()) return;

        // We would try to collect the loot, if one is collected, it would be removed from the list
        List<ItemStack> items = new ArrayList<>(event.getItems());
        plugin.processingPlant.collectLoot(player, items);
        event.setItems(items); // set remaining items
    }

    @Override
    public Map<String, HandlerList> getEventHandlers() {
        return Collections.singletonMap("DropQueuePushEvent", DropQueuePushEvent.getHandlerList());
    }
}
