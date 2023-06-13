package dev.anhcraft.oreprocessor.storage;

import org.jetbrains.annotations.NotNull;

public class TrackedPlayerData {
    private final PlayerDataConfigV1 playerData;
    private long loadTime;

    public TrackedPlayerData(@NotNull PlayerDataConfigV1 playerData, long loadTime) {
        this.playerData = playerData;
        this.loadTime = loadTime;
    }

    @NotNull
    public PlayerDataConfigV1 getPlayerData() {
        return playerData;
    }

    public long getLoadTime() {
        return Math.abs(loadTime);
    }

    public boolean isShortTerm() {
        return loadTime > 0;
    }

    public void setShortTerm() {
        loadTime = System.currentTimeMillis();
    }

    public void setLongTerm() {
        loadTime = -System.currentTimeMillis();
    }
}
