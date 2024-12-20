package dev.anhcraft.oreprocessor.integration.adder;

import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class VanillaBridge implements ItemCustomizer {
    private final Set<UMaterial> customMaterials;

    public VanillaBridge() {
        customMaterials = Arrays.stream(Material.values())
          .filter(m -> !m.isLegacy())
          .map(UMaterial::of)
          .collect(Collectors.toSet());
    }

    @Override
    public MaterialClass getMaterialClass() {
        return MaterialClass.VANILLA;
    }

    @Override
    public Set<UMaterial> getMaterials() {
        return customMaterials;
    }

    @Override
    public @Nullable ItemStack buildItem(@NotNull UMaterial material) {
        return new ItemStack(material.asBukkit());
    }

    @Override
    public @Nullable UItemStack identifyItem(@NotNull ItemStack item) {
        // only raw item is vanilla, otherwise it might be custom item
        return item.hasItemMeta() ? null : UItemStack.of(item);
    }

    @Override
    public @Nullable UMaterial identifyMaterial(@NotNull ItemStack item) {
        // only raw item is vanilla, otherwise it might be custom item
        return item.hasItemMeta() ? null : UMaterial.of(item.getType());
    }

    @Override
    public @Nullable UMaterial identifyMaterial(@NotNull Block block) {
        return UMaterial.of(block.getType());
    }

    @Override
    public @Nullable List<ItemStack> getLoot(@NotNull Block block, @NotNull ItemStack tool) {
        return new ArrayList<>(block.getDrops(tool));
    }
}
