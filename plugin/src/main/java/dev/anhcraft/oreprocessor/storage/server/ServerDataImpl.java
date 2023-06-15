package dev.anhcraft.oreprocessor.storage.server;

import dev.anhcraft.oreprocessor.api.data.ServerData;
import dev.anhcraft.oreprocessor.api.data.stats.Statistics;
import dev.anhcraft.oreprocessor.storage.stats.StatisticsImpl;
import org.jetbrains.annotations.NotNull;

public class ServerDataImpl implements ServerData {
    private final ServerDataConfig config;

    public ServerDataImpl(ServerDataConfig config) {
        this.config = config;
    }

    @Override
    public boolean isDirty() {
        return config.dirty.get();
    }

    @Override
    public int getDataVersion() {
        return config.dataVersion;
    }

    @Override
    public @NotNull Statistics getCumulativeStats() {
        return new StatisticsImpl(config.dirty, config.getStats().getCumulativeStats());
    }

    @Override
    public @NotNull Statistics getHourlyStats(long timestamp) {
        return new StatisticsImpl(config.dirty, config.getStats().getHourlyStat(timestamp));
    }
}