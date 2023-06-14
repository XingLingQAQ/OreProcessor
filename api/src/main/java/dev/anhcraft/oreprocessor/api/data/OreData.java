package dev.anhcraft.oreprocessor.api.data;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface OreData extends ModifiableData {
    int getThroughput();

    void setThroughput(int amount);

    int getCapacity();

    void setCapacity(int amount);

    @NotNull
    Set<Material> getFeedstock();

    void addFeedstock(@NotNull Material material, int amount);

    int countFeedstock(@NotNull Material material);

    int countAllFeedstock();

    @NotNull
    Set<Material> getProducts();

    int addProduct(@NotNull Material material, int expectedAmount, boolean force);

    int takeProduct(@NotNull Material material, int expectedAmount);

    boolean testAndTakeProduct(@NotNull Material material, int expectedAmount, @NotNull Function<Integer, Boolean> function);

    int countProduct(@NotNull Material material);

    int countAllProducts();

    default boolean isFull() {
        return countAllFeedstock() + countAllProducts() >= getCapacity();
    }

    @ApiStatus.Internal
    void process(int throughputMultiplier, @NotNull UnaryOperator<Material> function);
}