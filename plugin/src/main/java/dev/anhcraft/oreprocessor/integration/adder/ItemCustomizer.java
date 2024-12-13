package dev.anhcraft.oreprocessor.integration.adder;

import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface ItemCustomizer {
    MaterialClass getMaterialClass();

    Set<UMaterial> getCustomMaterials();

    @Nullable
    ItemStack buildItem(@NotNull UMaterial material);

    @Nullable
    UItemStack identifyItem(@NotNull ItemStack item);

    @Nullable
    UMaterial identifyMaterial(@NotNull ItemStack item);

    @Nullable
    UMaterial identifyMaterial(@NotNull Block block);

    @Nullable
    List<ItemStack> getLoot(@NotNull Block block, @NotNull ItemStack tool);
}
