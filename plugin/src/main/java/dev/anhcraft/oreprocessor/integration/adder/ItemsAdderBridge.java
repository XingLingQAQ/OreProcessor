package dev.anhcraft.oreprocessor.integration.adder;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.integration.Integration;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemsAdderBridge implements Integration, Listener, ItemCustomizer {
    private final OreProcessor plugin;

    public ItemsAdderBridge(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void handleCustomBlockBreakEvent(CustomBlockBreakEvent event) {
        // It is risky/complex to modify the loot here
        // ItemsAdder not only provide custom block but also custom loots to existing vanilla blocks
        // The safest approach is using the loot drop collector
        plugin.processingPlant.scheduleLootDropCollector(event.getPlayer(), event.getBlock());
    }

    @Override
    public MaterialClass getMaterialClass() {
        return MaterialClass.ITEMSADDER;
    }

    @Override
    public Set<UMaterial> getCustomMaterials() {
        return CustomStack.getNamespacedIdsInRegistry().stream().map(UMaterial::fromItemsAdder).collect(Collectors.toSet());
    }

    @Override
    public ItemStack buildItem(@NotNull UMaterial material) {
        CustomStack cs = CustomStack.getInstance(material.getIdentifier());
        return cs == null ? null : cs.getItemStack();
    }

    @Override
    public UItemStack identifyItem(@NotNull ItemStack item) {
        CustomStack cs = CustomStack.byItemStack(item);
        return cs == null ? null : new UItemStack(UMaterial.fromItemsAdder(cs.getNamespacedID()), item.getAmount());
    }

    @Override
    public UMaterial identifyMaterial(@NotNull ItemStack item) {
        CustomStack cs = CustomStack.byItemStack(item);
        return cs == null ? null : UMaterial.fromItemsAdder(cs.getNamespacedID());
    }

    @Override
    public @Nullable UMaterial identifyMaterial(@NotNull Block block) {
        CustomBlock cb = CustomBlock.byAlreadyPlaced(block);
        return cb == null ? null : UMaterial.fromItemsAdder(cb.getNamespacedID());
    }

    @Override
    public @Nullable List<ItemStack> getLoot(@NotNull Block block, @NotNull ItemStack tool) {
        CustomBlock cb = CustomBlock.byAlreadyPlaced(block);
        return cb == null ? null : cb.getLoot(tool, false);
    }
}
