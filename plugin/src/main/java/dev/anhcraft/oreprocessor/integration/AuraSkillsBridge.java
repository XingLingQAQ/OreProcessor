package dev.anhcraft.oreprocessor.integration;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
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

public class AuraSkillsBridge implements Integration, Listener, EventDebugger {
  private final OreProcessor plugin;

  public AuraSkillsBridge(OreProcessor plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  private void onDropLoot(LootDropEvent event) {
    Player player = event.getPlayer();

    // We would try to collect the loot, if one is collected, it would be removed from the list
    List<ItemStack> itemStacks = new ArrayList<>(1);
    itemStacks.add(event.getItem());
    plugin.processingPlant.collectLoot(player, itemStacks);

    if (itemStacks.isEmpty())
      event.setCancelled(true);
  }

  @Override
  public Map<String, HandlerList> getEventHandlers() {
    return Collections.singletonMap("LootDropEvent", LootDropEvent.getHandlerList());
  }
}
