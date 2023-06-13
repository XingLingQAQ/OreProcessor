package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.OreTransform;
import dev.anhcraft.oreprocessor.api.data.OreData;
import org.bukkit.scheduler.BukkitRunnable;

public class MineralProcessingTask extends BukkitRunnable {
    @Override
    public void run() {
        OreProcessor.getInstance().playerDataManager.streamData((uuid, playerData) -> {
            for (String oreId : playerData.listOreIds()) {
                Ore ore = OreProcessor.getApi().getOre(oreId);
                if (ore == null) continue;

                OreTransform oreTransform = ore.getBestTransform(uuid);
                OreData oreData = playerData.getOreData(oreId);
                if (oreData == null) continue;

                oreData.process(1, oreTransform::convert);
            }
        });
    }
}
