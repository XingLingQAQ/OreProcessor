package dev.anhcraft.oreprocessor.integration;

import dev.anhcraft.oreprocessor.OreProcessor;
import net.advancedplugins.ae.impl.utils.protection.events.FakeAdvancedBlockBreakEvent;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.Map;

public class AdvancedEnchantmentBridge implements Integration, EventDebugger {
    private final OreProcessor plugin;

    public AdvancedEnchantmentBridge(OreProcessor plugin) {
        this.plugin = plugin;
    }

    // NOTE: FakeAdvancedBlockBreakEvent is unreliable; However, AE does trigger BlockBreakEvent
    // for successive breaking (e.g. trench); so that case would be handled in BlockEventListener

    @Override
    public Map<String, HandlerList> getEventHandlers() {
        return Collections.singletonMap("FakeAdvancedBlockBreakEvent", FakeAdvancedBlockBreakEvent.getHandlerList());
    }
}
