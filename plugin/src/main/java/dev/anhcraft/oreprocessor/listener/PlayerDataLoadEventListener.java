package dev.anhcraft.oreprocessor.listener;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.OreTransform;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.event.AsyncPlayerDataLoadEvent;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.storage.stats.StatisticHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class PlayerDataLoadEventListener implements Listener {
  private final OreProcessor plugin;

  public PlayerDataLoadEventListener(OreProcessor plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerDataLoad(AsyncPlayerDataLoadEvent event) {
    for (String oreId : event.getData().listOreIds()) {
      OreData oreData = event.getData().requireOreData(oreId);

      int throughput = oreData.getThroughput();
      int defaultThroughput = OreProcessor.getApi().getDefaultThroughput();
      if (throughput < defaultThroughput) {
        oreData.setThroughput(defaultThroughput);
        plugin.debug("Upgrade %s's %s throughput to default value: %d → %d", event.getPlayerId(), oreId, throughput, defaultThroughput);
      }

      int capacity = oreData.getCapacity();
      int defaultCapacity = OreProcessor.getApi().getDefaultCapacity();
      if (capacity < defaultCapacity) {
        oreData.setCapacity(defaultCapacity);
        plugin.debug("Upgrade %s's %s capacity to default value: %d → %d", event.getPlayerId(), oreId, capacity, defaultCapacity);
      }
    }

    long hibernationStart = event.getData().getHibernationStart();
    if (hibernationStart > 0) {
      long hibernationTime = (System.currentTimeMillis() - hibernationStart) / 1000;
      if (hibernationTime > 0) {
        int mul = (int) (hibernationTime / OreProcessor.getApi().getProcessingInterval());
        plugin.debug("Processing hibernated materials for %s, time = %ds, multi = x%d", event.getPlayerId(), hibernationTime, mul);

        if (!plugin.mainConfig.behaviourSettings.disableOfflineProcessing) {
          for (String oreId : event.getData().listOreIds()) {
            OreTransform oreTransform = OreProcessor.getApi().requireOre(oreId).getBestTransform(event.getPlayerId());
            Map<UMaterial, Integer> summary = event.getData().requireOreData(oreId).process(mul, oreTransform::convert);
            if (summary.isEmpty()) continue;
            int processed = summary.values().stream().reduce(0, Integer::sum);

            StatisticHelper.increaseProductCount(oreId, processed, event.getData());
            StatisticHelper.increaseProductCount(oreId, processed, OreProcessor.getApi().getServerData());
            plugin.debug(2, String.format(
              "Processed x%d %s for %s using transform #%s",
              processed, oreId, event.getPlayerId(), oreTransform.getId()
            ));
            for (Map.Entry<UMaterial, Integer> e : summary.entrySet()) {
              plugin.pluginLogger.scope("offline-processing")
                .add("player", event.getPlayerId())
                .add("hibernation", hibernationTime)
                .add("multiplier", mul)
                .add("ore", oreId)
                .add("transform", oreTransform.getId())
                .add("product", e.getKey())
                .add("amount", e.getValue())
                .flush();
            }
          }
        }

        // then reset hibernation to prevent any unexpected accidents causing duplication
        event.getData().setHibernationStart(0);
      }
    }

    if (plugin.mainConfig.purgeStats.maxPlayerRecords > 0) {
      plugin.debug(String.format(
        "Removed %d oldest statistics records from player %s",
        event.getData().purgeHourlyStats(plugin.mainConfig.purgeStats.maxPlayerRecords), event.getPlayerId()
      ));
    }
  }
}
