package dev.anhcraft.oreprocessor.api;

import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.data.ServerData;
import dev.anhcraft.oreprocessor.api.integration.ShopProviderType;
import dev.anhcraft.oreprocessor.api.upgrade.UpgradeLevel;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface OreProcessorApi {
    /**
     * Gets all registered ore IDs.
     * @return All ore IDs
     */
    @NotNull
    Set<String> getOres();

    /**
     * Gets the ore with given ID
     * @param id The ore ID
     * @return The ore
     */
    @Nullable
    Ore getOre(@NotNull String id);

    /**
     * Requires an ore with the given ID.
     * @param id The ore ID
     * @return The ore
     * @throws NullPointerException if no ore found
     */
    @NotNull
    default Ore requireOre(@NotNull String id) {
        return Objects.requireNonNull(getOre(id), "Ore not found: " + id);
    }

    /**
     * Gets the ore which is allowed for the given block material.
     * @param block The block material
     * @return The ore
     */
    @Nullable
    Ore getBlockOre(UMaterial block);

    /**
     * Gets all ores that accept the given feedstock.
     * @param feedstock The feedstock
     * @return Found ores
     */
    @NotNull
    Collection<Ore> getOresAllowFeedstock(UMaterial feedstock);

    /**
     * Gets all materials allowed in the given storage.<br>
     * If not specified, {@code null} is returned.<br>
     * The return list can be empty.
     * @param id storage ID
     * @return allowed materials
     */
    @Nullable
    Set<UMaterial> getStorageFilter(String id);

    /**
     * Gets all suitable locations to store the given item.<br>
     * This guarantees at least one location.<br>
     * If not specified, {@code null} is returned.<br>
     * @param item The item
     * @return The suitable storages
     */
    @Nullable
    List<Ore> getStorageAllowItem(UMaterial item);

    /**
     * Gets the processing interval.
     * @return The processing interval
     */
    float getProcessingInterval();

    /**
     * Gets the default capacity.
     * @return The default capacity
     */
    int getDefaultCapacity();

    /**
     * Gets the default throughput.
     * @return The default throughput
     */
    int getDefaultThroughput();

    /**
     * Gets throughput per minute based on current processing speed.
     * @param throughput The current throughput
     * @return The throughput per minute
     */
    int getThroughputPerMinute(int throughput);

    /**
     * Gets player data of an online player.<br>
     * For any online player, the data always exists during their session.
     * @param player The player
     * @return The player data
     */
    @NotNull
    PlayerData getPlayerData(@NotNull Player player);

    /**
     * Gets player data of a player from the cache without fetching or blocking.<br>
     * If the player is online, the data always exists. If the player is offline and his data was recently fetched,
     * then this returns immediately. Otherwise, this returns {@code null}.<br>
     * To attempt to fetch the data of any player, use {@link #requirePlayerData(UUID)}.
     * @param id The player ID
     * @return The player data
     */
    @NotNull
    Optional<PlayerData> getPlayerData(@NotNull UUID id);

    /**
     * Requires the player data from the cache or fetches if not exists yet.<br>
     * This returns a {@link CompletableFuture} which will be done asynchronously. For online player and offline player
     * whose data exists in cache, the result is immediately returned. Otherwise, it will take some time.
     * @param id The player ID
     * @return The player data
     */
    @NotNull
    CompletableFuture<PlayerData> requirePlayerData(@NotNull UUID id);

    /**
     * Gets the server data.
     * @return The server data
     */
    @NotNull
    ServerData getServerData();

    /**
     * Gets the next throughput upgrade.
     * @param currentThroughput The current throughput
     * @return The next throughput upgrade
     */
    @Nullable
    UpgradeLevel getNextThroughputUpgrade(int currentThroughput);

    /**
     * Gets the next capacity upgrade.
     * @param currentCapacity The current capacity
     * @return The next capacity upgrade
     */
    @Nullable
    UpgradeLevel getNextCapacityUpgrade(int currentCapacity);

    /**
     * Gets the shop provider.
     * @return The shop provider
     */
    ShopProviderType getShopProvider();

    /**
     * Gets the material of the given block.
     * @param block The block
     * @return The material
     */
    @Contract("null -> null")
    @Nullable
    UMaterial identifyMaterial(@Nullable Block block);

    /**
     * Gets the material of the given item stack.
     * @param itemStack The item stack
     * @return The material
     */
    @Contract("null -> null")
    @Nullable
    UMaterial identifyMaterial(@Nullable ItemStack itemStack);

    /**
     * Gets the {@link UItemStack} of the given item stack.
     * @param itemStack The item stack
     * @return The item
     */
    @Contract("null -> null")
    @Nullable
    UItemStack identifyItem(@Nullable ItemStack itemStack);

    /**
     * Builds an {@link ItemStack} from the given {@link UMaterial}<br>
     * For vanilla materials, this guarantees that the item returned is not-null. Otherwise, the result may be
     * null due to several reasons such as the associated 3rd plugin is unavailable, the given material is not
     * registered, or whatever caused it failed to create the item.
     * @param material The material
     * @return The item
     */
    @Contract("null, _ -> null")
    @Nullable
    ItemStack buildItem(@Nullable UMaterial material, int amount);

    /**
     * Builds an {@link ItemStack} from the given {@link UMaterial}<br>
     * Calling this method is the same as calling to {@link #buildItem(UMaterial, int)} with an amount of 1.
     * @param material The material
     * @return The item
     * @see #buildItem(UMaterial, int)
     */
    @Contract("null -> null")
    @Nullable
    default ItemStack buildItem(@Nullable UMaterial material) {
        return buildItem(material, 1);
    }

    /**
     * Builds an {@link ItemStack} from the given {@link UItemStack}
     * Calling this method is the same as calling to {@link #buildItem(UMaterial, int)}
     * @param itemStack The item stack
     * @return The item
     * @see #buildItem(UMaterial, int)
     */
    @Contract("null -> null")
    @Nullable
    default ItemStack buildItem(@Nullable UItemStack itemStack) {
        if (itemStack == null) return null;
        return buildItem(itemStack.material(), itemStack.amount());
    }

    /**
     * Gets the loot of the given block.
     * @param block The block
     * @param tool The tool used to mine the block
     * @return a list of items stack; if {@code null}, the block is not an ore
     */
    @Nullable
    List<ItemStack> getLoot(@NotNull Block block, @NotNull ItemStack tool);
}
