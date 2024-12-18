package dev.anhcraft.oreprocessor.integration;

import dev.anhcraft.oreprocessor.OreProcessor;
import net.advancedplugins.ae.impl.utils.protection.events.FakeAdvancedBlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.Map;

public class AdvancedEnchantmentBridge implements Integration, Listener, EventDebugger {
    private final OreProcessor plugin;

    public AdvancedEnchantmentBridge(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // This assists enchantments that performs fake block-breaking such as
    // - Break multiple blocks (blast, explosive, ...)
    // - Break a target block not triggered by mining
    // For actual mining, that case is already covered by BlockEventListener#onBreak
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onFakeBreak(FakeAdvancedBlockBreakEvent event) {
        plugin.processingPlant.scheduleLootDropCollector(event.getPlayer(), event.getBlock());
    }

    @Override
    public Map<String, HandlerList> getEventHandlers() {
        return Collections.singletonMap("FakeAdvancedBlockBreakEvent", FakeAdvancedBlockBreakEvent.getHandlerList());
    }
}
